package ch.uzh.ifi.csg.contract.service.serialization;

import java.lang.reflect.Type;

/**
 * Created by flo on 16.06.17.
 */

public interface SerializationService
{
    String serialize(Object data);
    <T> T deserialize(String data, Type type);
}
