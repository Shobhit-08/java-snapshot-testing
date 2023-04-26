package au.com.origin.snapshots;

import au.com.origin.snapshots.comparators.SnapshotComparator;
import au.com.origin.snapshots.reporters.SnapshotReporter;
import au.com.origin.snapshots.serializers.SnapshotSerializer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class Expect {
  private final SnapshotVerifier snapshotVerifier;
  private final Method testMethod;

  private SnapshotSerializer snapshotSerializer;
  private SnapshotComparator snapshotComparator;
  private List<SnapshotReporter> snapshotReporters;
  private String scenario;

  private final Map<String, String> headers = new HashMap<>();

  public static Expect of(SnapshotVerifier snapshotVerifier, Method method) {
    return new Expect(snapshotVerifier, method);
  }

  /**
   * Make an assertion on the given input parameters against what already exists
   *
   * <p>If you were previously using varargs and see an error - you can fix the error using
   * "toMatchSnapshotLegacy", however, a better approach is to use the ".scenario()" feature as
   * future versions of this library will most likely remove the legacy implementation completely.
   *
   * @param object snapshot object
   */
  public void toMatchSnapshot(Object object) {
    SnapshotContext snapshotContext = snapshotVerifier.expectCondition(testMethod, object);
    if (snapshotSerializer != null) {
      snapshotContext.setSnapshotSerializer(snapshotSerializer);
    }
    if (snapshotComparator != null) {
      snapshotContext.setSnapshotComparator(snapshotComparator);
    }
    if (snapshotReporters != null) {
      snapshotContext.setSnapshotReporters(snapshotReporters);
    }
    if (scenario != null) {
      snapshotContext.setScenario(scenario);
    }
    snapshotContext.header.putAll(headers);

    snapshotContext.checkValidContext();

    snapshotContext.toMatchSnapshot();
  }

  /**
   * Normally a snapshot can be applied only once to a test method.
   *
   * <p>For Parameterized tests where the same method is executed multiple times you can supply the
   * scenario() to overcome this restriction. Ensure each scenario is unique.
   *
   * @param scenario - unique scenario description
   * @return Snapshot
   */
  public Expect scenario(String scenario) {
    this.scenario = scenario;
    return this;
  }

  /**
   * Apply a custom serializer for this snapshot
   *
   * @param serializer your custom serializer
   * @return Snapshot
   */
  public Expect serializer(SnapshotSerializer serializer) {
    this.snapshotSerializer = serializer;
    return this;
  }

  /**
   * Apply a custom serializer for this snapshot
   *
   * @param name - the {name} attribute serializer.{name} from snapshot.properties
   * @return Snapshot
   */
  public Expect serializer(String name) {
    this.snapshotSerializer = SnapshotProperties.getInstance("serializer." + name);
    return this;
  }

  /**
   * Apply a custom comparator for this snapshot
   *
   * @param comparator your custom comparator
   * @return Snapshot
   */
  public Expect comparator(SnapshotComparator comparator) {
    this.snapshotComparator = comparator;
    return this;
  }

  /**
   * Apply a custom comparator for this snapshot
   *
   * @param name the {name} attribute comparator.{name} from snapshot.properties
   * @return Snapshot
   */
  public Expect comparator(String name) {
    this.snapshotComparator = SnapshotProperties.getInstance("comparator." + name);
    return this;
  }

  /**
   * Apply a list of custom reporters for this snapshot This will replace the default reporters
   * defined in the config
   *
   * @param reporters your custom reporters
   * @return Snapshot
   */
  public Expect reporters(SnapshotReporter... reporters) {
    this.snapshotReporters = Arrays.asList(reporters);
    return this;
  }

  /**
   * Apply a list of custom reporters for this snapshot This will replace the default reporters
   * defined in the config
   *
   * @param name the {name} attribute reporters.{name} from snapshot.properties
   * @return Snapshot
   */
  public Expect reporters(String name) {
    this.snapshotReporters = SnapshotProperties.getInstances("reporters." + name);
    return this;
  }

  /**
   * Apply a custom serializer for this snapshot.
   *
   * @param serializer your custom serializer
   * @return this
   * @see au.com.origin.snapshots.serializers.SnapshotSerializer
   *     <p>Example implementations
   * @see au.com.origin.snapshots.serializers.ToStringSnapshotSerializer
   * @see au.com.origin.snapshots.serializers.Base64SnapshotSerializer
   */
  @SneakyThrows
  public Expect serializer(Class<? extends SnapshotSerializer> serializer) {
    this.snapshotSerializer = serializer.getConstructor().newInstance();
    return this;
  }

  /**
   * Add anything you like to the snapshot header.
   *
   * <p>These custom headers can be used in serializers, comparators or reporters to change how they
   * behave.
   *
   * @param key key
   * @param value value
   * @return Expect
   */
  public Expect header(String key, String value) {
    headers.put(key, value);
    return this;
  }
}
