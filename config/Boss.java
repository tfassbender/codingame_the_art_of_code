import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

class Player {

  private Set<Region> regions;
  private List<FieldWithConnection> fields;

  private boolean priorityIsLower;
  private long deployableTroops;
  private int startingFieldsLeft;
  private TurnType turnType;

  private List<Action> actions = new ArrayList<Action>();

  public static void main(String[] args) {
    Player agent = new Player();
    Scanner in = new Scanner(System.in);

    agent.firstRead(in);

    while (true) {
      agent.read(in);

      agent.think();

      agent.print(System.out);
    }
  }

  public void firstRead(Scanner sc) {
    fields = new ArrayList<FieldWithConnection>();
    regions = new HashSet<Region>();
    Map<Integer, Pair<Integer, Set<Field>>> regionFields = new HashMap<>();

    // next line: one integer - the NUMBER_OF_REGIONS
    int numberOfRegions = sc.nextInt();
    // next NUMBER_OF_REGIONS inputs:
    // - one line: two integers - region id and the number of bonus troops for this region
    for (int i = 0; i < numberOfRegions; i++) {
      int regionId = sc.nextInt();
      int bonusTroops = sc.nextInt();

      regionFields.put(regionId, Pair.of(bonusTroops, new HashSet<Field>()));
    }

    // first line: one integer - the NUMBER_OF_FIELDS in the map
    int numberOfFields = sc.nextInt();
    // next NUMBER_OF_FIELDS inputs:
    // - one line: one integer - the id of the region, that the field belongs to
    for (int i = 0; i < numberOfFields; i++) {
      int fieldId = sc.nextInt();
      int regionId = sc.nextInt();

      FieldWithConnection field = new FieldWithConnection(fieldId);
      fields.add(field);

      regionFields.get(regionId).getValue().add(field);
    }

    Collections.sort(fields);

    // next line: one integer - the NUMBER_OF_CONNECTIONS between fields
    int numberOfConnections = sc.nextInt();
    for (int i = 0; i < numberOfConnections; i++) {
      // next NUMBER_OF_CONNECTIONS lines: two integers - the SOURCE_ID and the TARGET_ID of the
      // fields that are connected (bidirectional)
      int sourceId = sc.nextInt();
      int targetId = sc.nextInt();

      FieldWithConnection source = fields.get(sourceId);
      FieldWithConnection target = fields.get(targetId);

      source.addConnection(target);
      target.addConnection(source);
    }

    for (Integer regionId : regionFields.keySet()) {
      regions.add(
          new Region(regionFields.get(regionId).getValue(), regionFields.get(regionId).getKey()));
    }
    priorityIsLower = sc.nextLine().equals("LOWER"); // UPPER OR LOWER
  }

  public void read(Scanner sc) {
    Owner[] owners = {Owner.NEUTRAL, Owner.PLAYER_1, Owner.PLAYER_2};

    // TODO check why this next line is/was required
    sc.nextLine();

    // first line: one string - the name of the turn type (CHOOSE_STARTING_FIELDS, DEPLOY_TROOPS or
    // MOVE_TROOPS)
    turnType = TurnType.valueOf(sc.nextLine());
    System.err.println(turnType.name());

    // next line: two integers - the number of the fields that are held by each player (your input
    // is always first)
    long playersFields = sc.nextLong();
    long otherPlayersFields = sc.nextLong();

    // next line: two integers - the number of troops that each player can deploy (your input is
    // always first; 0 in all turn types but DEPLOY_TROOPS)
    deployableTroops = sc.nextLong();
    long enemyDeployableTroops = sc.nextLong();

    startingFieldsLeft = sc.nextInt();
    int enemyStartingFieldsLeft = sc.nextInt();

    for (int i = 0; i < fields.size(); i++) {
      int id = sc.nextInt(); // FIELD_ID is the id of the Field
      int troops = sc.nextInt(); // NUMBER_OF_TROOPS in this field
      int owner =
          sc
              .nextInt(); // OWNER of this field (1 if the field is controlled by you; 2 if it's
                          // controlled by the opponent player; 0 if it's neutral)

      FieldWithConnection field = fields.get(id);
      field.set(owners[owner], troops);
    }
  }

  private void think() {
    actions.clear();

    switch (turnType) {
      case CHOOSE_STARTING_FIELDS:
        thinkChooseStarting();
        break;
      case DEPLOY_TROOPS:
        thinkDeploy();
        break;
      case MOVE_TROOPS:
        thinkMove();
        break;
    }
  }

  private void thinkChooseStarting() {
    // just do random (or wait if we can't choose another)
    actions.add(new Action(Action.Type.RANDOM));
  }

  private void thinkDeploy() {
    // place troops at random fields
    List<FieldWithConnection> myFields = getFieldsForOwner(Owner.PLAYER_1);

    // tmp solution, while 0 fields != gameEnd
    if (myFields.size() == 0) {
      actions.add(new Action(Action.Type.WAIT));
      return;
    }

    while (deployableTroops > 0) {
      Field field = myFields.get((int) (Math.random() * myFields.size()));

      actions.add(new Action(Action.Type.DEPLOY, field.id, 1));

      deployableTroops--;
    }
  }

  private void thinkMove() {
    // - attack with every single unit
    // - do some random movements within the army

    List<FieldWithConnection> myFields = getFieldsForOwner(Owner.PLAYER_1);

    for (FieldWithConnection myField : myFields) {
      List<Field> toAssist = myField.getConnectedFieldsWithSameOwner(true);
      List<Field> toHarm = myField.getConnectedFieldsWithSameOwner(false);
      List<Field> targets = toHarm.size() > 0 ? toHarm : toAssist;

      for (int i = 0; i < targets.size() && myField.getTroops() != 0; i++) {
        // at least 1 troop
        int troops = (int) (Math.random() * myField.getTroops() - 1) + 1;

        // use the rest
        if (i == targets.size() - 1) {
          troops = myField.getTroops();
        }

        actions.add(new Action(Action.Type.MOVE, myField.id, targets.get(i).id, troops));

        myField.adjustTroops(-troops);
      }
    }
  }

  private void print(PrintStream out) {
    // randomize order of the actions
    Collections.shuffle(actions);

    List<String> actionsAsStrings =
        actions.stream().map(action -> action.toString()).collect(Collectors.toList());

    out.println(String.join(";", actionsAsStrings));
  }

  private List<FieldWithConnection> getFieldsForOwner(Owner owner) {
    return fields.stream().filter(field -> field.getOwner() == owner).collect(Collectors.toList());
  }

  public static class Vector2D implements Cloneable {

    public double x;
    public double y;

    public static final Vector2D NAN_VEC = new Vector2D(Double.NaN, Double.NaN);
    public static final Vector2D NULL_VEC = new Vector2D(0, 0);

    /*public enum Axis {
    	X,
    	Y;
    }*/

    public Vector2D() {}

    /** Crate a new Vector2D with x and y components. */
    public Vector2D(double x, double y) {
      this.x = x;
      this.y = y;
    }

    public Vector2D(double... val) {
      if (val.length != 2) {
        // throw new LinearAlgebraException("A 2D Vector has 2 entries.");
      }
      x = val[0];
      y = val[1];
    }
    /**
     * Create a Vector2D by an angle (in degree). An angle of 0° results in (x, y) = (1, 0); 90°
     * in (x, y) = (0, 1); ... The resulting vector has a length of 1.
     *
     * @param angleDegree The angle of the new vector in degree.
     */
    public Vector2D(double angleDegree) {
      this(Math.cos(angleDegree * Math.PI / 180), Math.sin(angleDegree * Math.PI / 180));
    }

    private Vector2D(Vector2D clone) {
      this.x = clone.x;
      this.y = clone.y;
    }

    /** Clone this Vector2D object. */
    @Override
    public Vector2D clone() {
      return new Vector2D(this);
    }

    @Override
    public String toString() {
      return "Vector2D[x: " + x + " y: " + y + "]";
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Vector2D) {
        Vector2D v = (Vector2D) obj;
        return Math.abs(x - v.x) < 1e-8 && Math.abs(y - v.y) < 1e-8;
      }
      return false;
    }

    /** Get this vector as 2D-Array. */
    public double[] asArray() {
      return new double[] {x, y};
    }

    /** The (euclidean) length of the Vector. */
    public double length() {
      return Math.hypot(x, y);
    }
    /**
     * The length of this vector in a given norm.
     *
     * @param norm The norm of the vector length.
     * @return The length of this vector in the given norm.
     */
    public double length(int norm) {
      if (norm == Integer.MAX_VALUE) {
        return Math.max(Math.abs(x), Math.abs(y));
      }
      return Math.pow(Math.pow(Math.abs(x), norm) + Math.pow(Math.abs(y), norm), 1.0 / norm);
    }

    /**
     * Rotate the Vector an angle (in degrees) resulting in a new Vector that is returned.
     *
     * @param degrees The angle to return the vector.
     * @return The new created vector.
     */
    public Vector2D rotate(double degrees) {
      return new Vector2D(getAngle() + degrees).setLength(length());
    }

    /**
     * Project the vector given as parameter on this vector.
     *
     * @param vec The vector that is to be projected on this vector.
     * @return The projected vector.
     */
    public Vector2D project(Vector2D vec) {
      return mult(scalar(vec) / Math.pow(length(), 2));
    }

    /**
     * Add another Vector2D to this vector resulting in a new Vector that is returned.
     *
     * @param vec The vector added to this vector.
     * @return The new created vector.
     */
    public Vector2D add(Vector2D vec) {
      return new Vector2D(x + vec.x, y + vec.y);
    }
    /**
     * Subtract another Vector3D from this vector resulting in a new Vector that is returned.
     *
     * @param vec The vector subtracted from this vector.
     * @return The new created vector.
     */
    public Vector2D sub(Vector2D vec) {
      return new Vector2D(x - vec.x, y - vec.y);
    }
    /**
     * Multiply this vector with a scalar resulting in a new Vector that is returned.
     *
     * @param scalar The scalar to multiply this vector with.
     * @return The new created vector.
     */
    public Vector2D mult(double scalar) {
      return new Vector2D(x * scalar, y * scalar);
    }

    /**
     * Check whether this vector is linearly dependent to the parameter vector.
     *
     * @param vec The checked vector.
     * @return True if the vectors are linearly dependent. False otherwise.
     */
    public boolean isLinearlyDependent(Vector2D vec) {
      double t1 = (x == 0 ? 0 : vec.x / x);
      double t2 = (y == 0 ? 0 : vec.y / y);
      return Math.abs(t1 - t2) < 1e-5 && t1 != 0; // all parameters t are equal and != 0
    }

    /**
     * Check whether the parameter vectors are linearly dependent.
     *
     * @param vectors The vectors that are checked.
     * @return True if the vectors are linearly dependent. False otherwise.
     */
    public boolean isLinearlyDependentVectors(Vector2D... vectors) {
      if (vectors.length < 2) {
        return false;
      } else if (vectors.length > 2) {
        // 3 or more vectors in the R^2 are always linearly dependent
        return true;
      } else {
        return vectors[0].isLinearlyDependent(vectors[1]);
      }
    }

    /**
     * Calculate the scalar product of this vector and the parameter vector.
     *
     * @param vec The vector to calculate the scalar with this vector.
     * @return The scalar of the vectors.
     */
    public double scalar(Vector2D vec) {
      return this.x * vec.x + this.y * vec.y;
    }

    /**
     * Create a new vector with the same direction but a different length as this vector.
     *
     * @param length The length of the new vector.
     * @return The new vector with a new length.
     */
    public Vector2D setLength(double length) {
      double len = length();
      return new Vector2D(x * length / len, y * length / len);
    }

    /**
     * Get the distance of this point's position vector to another point's position vector.
     *
     * @param p The second point's position vector.
     * @return The distance between the points.
     */
    public double distance(Vector2D p) {
      return Math.sqrt((this.x - p.x) * (this.x - p.x) + (this.y - p.y) * (this.y - p.y));
    }

    /** Change this vector to the new coordinates. */
    public void move(double x, double y) {
      this.x = x;
      this.y = y;
    }

    /**
     * Move a point's position vector in a direction (by a vector) and a distance.
     *
     * @param p The direction vector.
     * @param distance The distance to move the new vector
     * @return The new created vector.
     */
    public Vector2D moveTo(Vector2D p, double distance) {
      double d = distance(p);
      double dx = p.x - x;
      double dy = p.y - y;
      double coef = distance / d;
      return new Vector2D(x + dx * coef, y + dy * coef);
    }

    /**
     * Get the angle of this vector. Angle: 0° is right ((x, y) = (1, 0)); on clockwise (degree)
     */
    public double getAngle() {
      return ((Math.atan2(y, x) * 180 / Math.PI) + 720) % 360;
    }

    /** Get the angle of this vector as radiant. */
    public double getAngleRad() {
      return (Math.atan2(y, x) + 4 * Math.PI) % (2 * Math.PI);
    }

    /**
     * Get the angle difference of this vector to another vector.
     *
     * @param vec The other vector.
     * @return The angle difference of the two vectors (from 0° to 180°).
     */
    public double getAngleDeltaTo(Vector2D vec) {
      double delta = Math.abs(getAngle() - vec.getAngle());
      if (delta > 180) {
        delta = 360 - delta;
      }
      return delta;
    }

    /**
     * Get the vector from this point to another.
     *
     * @param vec The point to which the vector is calculated.
     * @return The vector from this points position vector to the other point.
     */
    public Vector2D vectorTo(Vector2D vec) {
      return new Vector2D(vec.x - x, vec.y - y);
    }

    /**
     * Checks whether a point (by its position vector) is in a given range of this point.
     *
     * @param p The point that is checked.
     * @param range The range used for the check.
     * @return True if the point is in the range of this point (distance <= range).
     */
    public boolean isInRange(Vector2D p, double range) {
      return p != this && distance(p) <= range;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }
  }

  public static class Pair<K, V> {

    public static <K, V> Pair<K, V> of(K key, V value) {
      return new Pair<K, V>(key, value);
    }

    private K key;
    private V value;

    private Pair(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }
  }

  /** The owner of a field */
  public enum Owner {
    PLAYER_1, //
    PLAYER_2, //
    NEUTRAL; //

    public Owner getOpponent() {
      if (this == PLAYER_1) {
        return PLAYER_2;
      }
      if (this == PLAYER_2) {
        return PLAYER_1;
      }
      return NEUTRAL;
    }
  }

  public enum TurnType {
    CHOOSE_STARTING_FIELDS, //
    DEPLOY_TROOPS, //
    MOVE_TROOPS; //
  }

  public static class FieldWithConnection extends Field implements Comparable<FieldWithConnection> {

    List<Field> reachable = new ArrayList<Field>();

    public FieldWithConnection(int id) {
      super(id);
    }

    public void set(Owner owner, int troops) {
      setOwner(owner);
      setTroops(troops);
    }

    public void adjustTroops(int delta) {
      setTroops(getTroops() + delta);
    }

    public void addConnection(Field field) {
      reachable.add(field);
    }

    public List<Field> getConnectedFieldsWithSameOwner(boolean sameOwner) {
      List<Field> fields = new ArrayList<Field>();

      for (Field field : reachable) {
        if ((field.getOwner() == getOwner()) == sameOwner) fields.add(field);
      }

      return fields;
    }

    @Override
    public int compareTo(FieldWithConnection o) {
      return Integer.compare(id, o.id);
    }
  }

  /** An action that was parsed from the player's output. */
  public static class Action {

    public static final int NO_SELECTION = -1;

    public static enum Type {
      PICK, //
      DEPLOY, //
      MOVE, //
      RANDOM, //
      WAIT; //

      @Override
      public String toString() {
        return name();
      }
    }

    private Type type;
    private int targetId; // the id of the target field (used in all move types)
    private int sourceId; // the id of the source field (only for MOVEMENT)
    private int
        numTroops; // the number of troops that are deployed / moved (must be greater than 0)

    private Owner owner;

    public Action(Type type) {
      this(type, NO_SELECTION, NO_SELECTION, 0);
    }

    public Action(Type type, int targetId) {
      this(type, NO_SELECTION, targetId, 0);
    }

    public Action(Type type, int targetId, int numTroops) {
      this(type, NO_SELECTION, targetId, numTroops);
    }

    public Action(Type type, int sourceId, int targetId, int numTroops) {
      this.type = type;
      this.sourceId = sourceId;
      this.targetId = targetId;
      this.numTroops = numTroops;
    }

    public Type getType() {
      return type;
    }

    public int getTargetId() {
      return targetId;
    }

    public int getSourceId() {
      return sourceId;
    }

    public int getNumTroops() {
      return numTroops;
    }

    public Owner getOwner() {
      return owner;
    }

    public Action setOwner(Owner owner) {
      this.owner = owner;
      return this;
    }

    @Override
    public String toString() {
      switch (type) {
        case PICK:
          return String.format("%s %d", type, targetId);
        case DEPLOY:
          return String.format("%s %d %d", type, targetId, numTroops);
        case MOVE:
          return String.format("%s %d %d %d", type, sourceId, targetId, numTroops);
        case RANDOM:
        case WAIT:
          return String.format("%s", type);
        default:
          throw new IllegalStateException("Invalid action state");
      }
    }
  }

  /** A set of (connected) fields, that form a region. */
  public static class Region {

    public final Set<Field> fields;
    public final int bonusTroops;
    public final int id;

    private static int REGION_ID_CNT = 0;

    // TODO maybe add a color or something to identify the region on the map

    public Region(Set<Field> fields, int bonusTroops) {
      this.fields = fields;
      this.bonusTroops = bonusTroops;

      this.id = REGION_ID_CNT++; // TODO find better names
    }

    public boolean isConqueredBy(Owner owner) {
      return fields.stream().allMatch(field -> field.getOwner() == owner);
    }

    public int getBonusTroops() {
      return bonusTroops;
    }

    public String getId() {
      return "R" + id;
    }

    public double distToPoint(Vector2D point, Map<Field, Vector2D> fieldPositions, int norm) {
      return fields
          .stream()
          .mapToDouble(f -> fieldPositions.get(f).sub(point).length(norm))
          .min()
          .getAsDouble();
    }
  }

  /** A field in the map, that contains troops and can be conquered. */
  public static class Field {

    public static final int NEUTRAL_CAMP_SIZE = 2;

    public final int id;
    private int troops;
    private Owner owner;

    public Field(int id) {
      this.id = id;
      owner = Owner.NEUTRAL;
      troops = NEUTRAL_CAMP_SIZE;
    }

    public int getTroops() {
      return troops;
    }

    protected void setTroops(int troops) {
      this.troops = troops;
    }

    public Owner getOwner() {
      return owner;
    }

    protected void setOwner(Owner owner) {
      this.owner = owner;
    }

    @Override
    public int hashCode() {
      return 13 * id;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Field)) {
        return false;
      }

      return id == ((Field) o).id;
    }
  }
}