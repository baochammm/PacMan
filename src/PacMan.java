import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize / 4;
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize / 4;
            } else if (this.direction == 'L') {
                this.velocityX = -tileSize / 4;
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = tileSize / 4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image organgeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;
    private Image scaredGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;
    private Image powerFoodImage;
    private Image cherryImage;

    // X = wall, O = skip, P = pac man, ' ' = food
    // Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X    C   X   C    X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X   S   X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O   C   bpo   C   O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X    C   S   C    X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    HashSet<Block> powerFoods;
    HashSet<Block> cherries;
    Block pacman;

    Timer gameLoop;
    Timer powerTimer;
    boolean ghostsScared = false;
    boolean isPaused = false;
    char[] directions = { 'U', 'D', 'L', 'R' }; // up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        organgeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("./scaredGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
        powerFoodImage = new ImageIcon(getClass().getResource("./powerFood.png")).getImage();
        cherryImage = new ImageIcon(getClass().getResource("./cherry.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        // how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); // 20fps (1000/50)
        gameLoop.start();

        powerTimer = new Timer(10000, e -> {
            ghostsScared = false;
            for (Block ghost : ghosts) {
                ghost.image = getGhostImage(ghost);
            }
        });
        powerTimer.setRepeats(false);
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        powerFoods = new HashSet<Block>();
        cherries = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;

                if (tileMapChar == 'X') { // block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                } else if (tileMapChar == 'b') { // blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'o') { // orange ghost
                    Block ghost = new Block(organgeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'p') { // pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'r') { // red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (tileMapChar == 'P') { // pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                } else if (tileMapChar == ' ') { // food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                } else if (tileMapChar == 'S') { // power pellets
                    Block powerFood = new Block(powerFoodImage, x + 8, y + 8, 16, 16);
                    powerFoods.add(powerFood);
                } else if (tileMapChar == 'C') { // Add a cherry
                    Block cherry = new Block(cherryImage, x + 8, y + 8, 16, 16);
                    cherries.add(cherry);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameOver) {
            // Draw the "Game Over" message
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String message = "Game Over";
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = (getHeight() / 2);
            g.drawString(message, x, y);

            g.setFont(new Font("Arial", Font.PLAIN, 24));
            String scoreMessage = "Score: " + score;
            int scoreX = (getWidth() - fm.stringWidth(scoreMessage)) / 2;
            g.drawString(scoreMessage, scoreX, y + 50);
            return; // Stop further rendering
        }
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        for (Block powerFood : powerFoods) {
            g.drawImage(powerFood.image, powerFood.x, powerFood.y, powerFood.width, powerFood.height, null);
        }

        for (Block cherry : cherries) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }

        // score
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), tileSize / 2, tileSize / 2);
        } else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize / 2, tileSize / 2);
        }
    }

    public void move() {
        if (gameOver) {
            return;
        }

        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Wrap-around logic for Pac-Man
        if (pacman.x < 0) {
            pacman.x = boardWidth - pacman.width; // Wrap from left to right
        } else if (pacman.x + pacman.width > boardWidth) {
            pacman.x = 0; // Wrap from right to left
        }

        // check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // check ghost collisions
        for (Block ghost : ghosts) {
            if (collision(pacman, ghost)) {
                if (ghostsScared) {
                    score += 200;
                    ghost.reset();
                } else {
                    lives -= 1;
                    if (lives == 0) {
                        gameOver = true;
                        return;
                    }
                    resetPositions();
                }
            }

            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        // check food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        Block powerFoodEaten = null;
        for (Block powerFood : powerFoods) {
            if (collision(pacman, powerFood)) {
                powerFoodEaten = powerFood;
                score += 50;
                activatePowerPellet();
            }
        }
        powerFoods.remove(powerFoodEaten);

        Block cherryEaten = null;
        for (Block cherry : cherries) {
            if (collision(pacman, cherry)) {
                cherryEaten = cherry;
                score += 77;
            }
        }
        cherries.remove(cherryEaten);

        if (foods.isEmpty() && cherries.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    private void activatePowerPellet() {
        ghostsScared = true;
        for (Block ghost : ghosts) {
            ghost.image = scaredGhostImage;
        }
        powerTimer.restart();
    }

    private Image getGhostImage(Block ghost) {
        if (ghostsScared) {
            return scaredGhostImage;
        }
        // Determine the correct original image based on start position
        int startCol = ghost.startX / tileSize;
        char tileMapChar = tileMap[ghost.startY / tileSize].charAt(startCol);
        switch (tileMapChar) {
            case 'b':
                return blueGhostImage;
            case 'o':
                return organgeGhostImage;
            case 'p':
                return pinkGhostImage;
            case 'r':
                return redGhostImage;
            default:
                return redGhostImage;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused) {
            move();
            repaint();
            if (gameOver) {
                gameLoop.stop();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }

        if (e.getKeyCode() == KeyEvent.VK_P) {
            isPaused = !isPaused; // Toggle pause state
        }

        if (!isPaused) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                pacman.updateDirection('U');
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                pacman.updateDirection('D');
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                pacman.updateDirection('L');
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                pacman.updateDirection('R');
            }

            if (pacman.direction == 'U') {
                pacman.image = pacmanUpImage;
            } else if (pacman.direction == 'D') {
                pacman.image = pacmanDownImage;
            } else if (pacman.direction == 'L') {
                pacman.image = pacmanLeftImage;
            } else if (pacman.direction == 'R') {
                pacman.image = pacmanRightImage;
            }
        }
    }
}
