package au.com.origin.snapshots;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import au.com.origin.snapshots.exceptions.SnapshotMatchException;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.mockito.ArgumentCaptor;

public class SnapshotUtils {

  public static <T> HashMap<String, List<LinkedHashMap<String, Object>>> extractArgs(
      T object, String methodName, SnapshotCaptor... snapshotCaptors) {
    List<ArgumentCaptor> captors = new ArrayList<>();
    Class[] classes = new Class[snapshotCaptors.length];

    int i = 0;
    for (SnapshotCaptor snapshotCaptor : snapshotCaptors) {
      classes[i] = snapshotCaptor.getParameterClass();
      captors.add(ArgumentCaptor.forClass(snapshotCaptor.getParameterClass()));
      i++;
    }

    return process(object, methodName, captors, classes, snapshotCaptors);
  }

  public static <T> HashMap<String, List<LinkedHashMap<String, Object>>> extractArgs(
      T object, String methodName, Class<?>... classes) {
    List<ArgumentCaptor> captors = new ArrayList<>();

    for (Class clazz : classes) {
      captors.add(ArgumentCaptor.forClass(clazz));
    }

    return process(object, methodName, captors, classes, null);
  }

  private static <T> HashMap<String, List<LinkedHashMap<String, Object>>> process(
      T object,
      String methodName,
      List<ArgumentCaptor> captors,
      Class[] classes,
      SnapshotCaptor[] snapshotCaptors) {
    HashMap<String, List<LinkedHashMap<String, Object>>> result = new HashMap<>();
    try {
      Parameter[] parameters =
          object.getClass().getDeclaredMethod(methodName, classes).getParameters();

      object
          .getClass()
          .getDeclaredMethod(methodName, classes)
          .invoke(
              verify(object, atLeastOnce()),
              captors.stream().map(ArgumentCaptor::capture).toArray());

      List<LinkedHashMap<String, Object>> extractedObjects = new ArrayList<>();

      int numberOfCall;

      if (captors.size() > 0) {
        numberOfCall = captors.get(0).getAllValues().size();

        for (int i = 0; i < numberOfCall; i++) {
          LinkedHashMap<String, Object> objectMap = new LinkedHashMap<>();

          int j = 0;
          for (ArgumentCaptor captor : captors) {
            Object value = captor.getAllValues().get(i);
            if (snapshotCaptors != null) {
              value = snapshotCaptors[j].removeIgnored(value);
            }
            objectMap.put(parameters[j].getName(), value);
            j++;
          }
          extractedObjects.add(objectMap);
        }
      }

      result.put(
          object.getClass().getSuperclass().getSimpleName() + "." + methodName, extractedObjects);
    } catch (Exception e) {
      throw new SnapshotMatchException(e.getMessage(), e.getCause());
    }

    return result;
  }

  public static void copyTestSnapshots() {
    try {
      FileUtils.copyDirectory(
          Paths.get("src/test/java/au/com/origin/snapshots/existing-snapshots").toFile(),
          Paths.get("src/test/java/au/com/origin/snapshots").toFile());
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Can't move files to __snapshots__ folder");
    }
  }

  public static void deleteTestSnapshots() {
    return;
//    try {
//      FileUtils.deleteDirectory(Paths.get("src/test/java/au/com/origin/snapshots/__snapshots__").toFile());
//    } catch (Exception e) {
//      e.printStackTrace();
//      throw new RuntimeException("Can't delete __snapshots__ folder");
//    }
  }
}
