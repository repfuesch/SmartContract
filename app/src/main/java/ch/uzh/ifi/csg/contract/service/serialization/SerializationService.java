package ch.uzh.ifi.csg.contract.service.serialization;

import java.lang.reflect.Type;

/**
 * Service used to serialize and deserialize objects
 */
public interface SerializationService
{
    /**
     * Serializes the provided object to a String
     *
     * @param data
     * @return
     */
    String serialize(Object data);

    /**
     * Deserializes the given String into the provided Java Type.
     *
     * @param data
     * @param type
     * @param <T>
     * @return
     */
    <T> T deserialize(String data, Type type);
}
