import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ParanormalVRGame {
    private Player player;
    private Ghost ghost;
    private VRController vrController;
    private GameState gameState;
    private SoundManager soundManager;

    public ParanormalVRGame() {
        player = new Player(0, 0, 0);
        ghost = new Ghost(10, 0, 10);
        vrController = new VRController();
        gameState = new GameState();
        soundManager = new SoundManager();
    }

    public void startGame() {
        gameState.setRunning(true);
        soundManager.playAmbientSound("haunted_house.wav");
        gameLoop();
    }

    private void gameLoop() {
        while (gameState.isRunning()) {
            update();
            render();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Game Over!");
    }

    private void update() {
        player.update(vrController.getInput());
        ghost.update(player.getPosition());
        checkInteractions();
    }

    private void render() {
        System.out.println("Player Position: " + player.getPosition().x + ", " +
                player.getPosition().y + ", " + player.getPosition().z);
        System.out.println("Ghost Position: " + ghost.getPosition().x + ", " +
                ghost.getPosition().y + ", " + ghost.getPosition().z);
    }

    private void checkInteractions() {
        float distance = player.getPosition().distanceTo(ghost.getPosition());
        if (distance < 1.0f) {
            System.out.println("Ghost caught you!");
            gameState.setRunning(false);
        }
    }

    public static void main(String[] args) {
        ParanormalVRGame game = new ParanormalVRGame();
        game.startGame();
    }
}

class Player {
    private Vector3 position;
    private float health;
    private float fearLevel;
    private Inventory inventory;

    public Player(float x, float y, float z) {
        position = new Vector3(x, y, z);
        health = 100.0f;
        fearLevel = 0.0f;
        inventory = new Inventory();
    }

    public void update(VRInput input) {
        move(input.getMovementVector());
        updateFearLevel();
        checkHealth();
    }

    private void move(Vector3 movement) {
        position.add(movement);
        if (position.y < 0) position.y = 0;
    }

    private void updateFearLevel() {
        // Simplified fear level update
        fearLevel += 0.05f;
        if (fearLevel > 100) fearLevel = 100;
    }

    private void checkHealth() {
        if (fearLevel >= 100) {
            health -= 1.0f;
            if (health <= 0) health = 0;
        }
    }

    public Vector3 getPosition() {
        return position;
    }
}

class Ghost {
    private Vector3 position;
    private float speed;
    private BehaviorState behavior;
    private float detectionRange;

    public Ghost(float x, float y, float z) {
        position = new Vector3(x, y, z);
        speed = 0.1f;
        behavior = BehaviorState.PATROLLING;
        detectionRange = 5.0f;
    }

    public void update(Vector3 playerPos) {
        float distance = position.distanceTo(playerPos);
        updateBehavior(distance);
        moveTowardsTarget(playerPos);
    }

    private void updateBehavior(float distance) {
        if (distance < detectionRange) {
            behavior = BehaviorState.CHASING;
        } else {
            behavior = BehaviorState.PATROLLING;
        }
    }

    private void moveTowardsTarget(Vector3 target) {
        if (behavior == BehaviorState.CHASING) {
            Vector3 direction = target.subtract(position).normalize();
            position.add(direction.multiply(speed));
        }
    }

    public Vector3 getPosition() {
        return position;
    }
}

class VRController {
    private Vector3 movementVector;
    private boolean triggerPressed;
    private float controllerRotation;

    public VRController() {
        movementVector = new Vector3(0, 0, 0);
        triggerPressed = false;
        controllerRotation = 0.0f;
    }

    public VRInput getInput() {
        updateControllerState();
        return new VRInput(movementVector, triggerPressed, controllerRotation);
    }

    private void updateControllerState() {
        // Simulate some basic movement for testing
        movementVector = new Vector3(0.05f, 0, 0.05f);
    }
}

class Vector3 {
    public float x, y, z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    public Vector3 multiply(float scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }

    public Vector3 normalize() {
        float length = (float)Math.sqrt(x*x + y*y + z*z);
        if (length == 0) return new Vector3(0, 0, 0);
        return new Vector3(x/length, y/length, z/length);
    }

    public float distanceTo(Vector3 other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
}

class VRInput {
    private Vector3 movement;
    private boolean trigger;
    private float rotation;

    public VRInput(Vector3 movement, boolean trigger, float rotation) {
        this.movement = movement;
        this.trigger = trigger;
        this.rotation = rotation;
    }

    public Vector3 getMovementVector() {
        return movement;
    }
}

class Inventory {
    private List<Item> items;
    private int maxCapacity;

    public Inventory() {
        items = new ArrayList<>();
        maxCapacity = 10;
    }

    public boolean addItem(Item item) {
        if (items.size() < maxCapacity) {
            items.add(item);
            return true;
        }
        return false;
    }
}

class Item {
    private String name;
    private float weight;
    private ItemType type;

    public Item(String name, float weight, ItemType type) {
        this.name = name;
        this.weight = weight;
        this.type = type;
    }
}

class GameState {
    private boolean running;
    private int score;
    private Difficulty difficulty;

    public GameState() {
        running = false;
        score = 0;
        difficulty = Difficulty.NORMAL;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }
}

class SoundManager {
    private Map<String, Sound> sounds;
    private float volume;

    public SoundManager() {
        sounds = new HashMap<>();
        volume = 1.0f;
    }

    public void playAmbientSound(String soundName) {
        Sound sound = sounds.get(soundName);
        if (sound == null) {
            sound = new Sound(soundName);
            sounds.put(soundName, sound);
        }
        sound.setVolume(volume);
        sound.play();
    }
}

class Sound {
    private String fileName;
    private boolean isPlaying;
    private float soundVolume;

    public Sound(String fileName) {
        this.fileName = fileName;
        this.isPlaying = false;
        this.soundVolume = 1.0f;
    }

    public void play() {
        isPlaying = true;
        System.out.println("Playing sound: " + fileName);
    }

    public void stop() {
        isPlaying = false;
    }

    public void setVolume(float volume) {
        this.soundVolume = volume;
    }
}

enum BehaviorState {
    PATROLLING,
    CHASING,
    ATTACKING
}

enum ItemType {
    KEY,
    WEAPON,
    COLLECTIBLE
}

enum Difficulty {
    EASY,
    NORMAL,
    HARD
}