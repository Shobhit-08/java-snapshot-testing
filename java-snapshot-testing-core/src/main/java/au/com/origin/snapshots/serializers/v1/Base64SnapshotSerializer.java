package au.com.origin.snapshots.serializers.v1;

import au.com.origin.snapshots.Snapshot;
import au.com.origin.snapshots.SnapshotSerializerContext;
import au.com.origin.snapshots.serializers.SerializerType;
import au.com.origin.snapshots.serializers.SnapshotSerializer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * This Serializer converts a byte[] into a base64 encoded string. If the input is not a byte[] it
 * will be converted using `.getBytes(StandardCharsets.UTF_8)` method
 */
public class Base64SnapshotSerializer implements SnapshotSerializer {
  private static final ToStringSnapshotSerializer toStringSnapshotSerializer =
      new ToStringSnapshotSerializer();

  @Override
  public Snapshot apply(Object object, SnapshotSerializerContext gen) {
    if (object == null) {
      toStringSnapshotSerializer.apply("", gen);
    }
    byte[] bytes =
        object instanceof byte[]
            ? (byte[]) object
            : object.toString().getBytes(StandardCharsets.UTF_8);
    String encoded = Base64.getEncoder().encodeToString(bytes);
    return toStringSnapshotSerializer.apply(encoded, gen);
  }

  @Override
  public String getOutputFormat() {
    return SerializerType.BASE64.name();
  }
}
