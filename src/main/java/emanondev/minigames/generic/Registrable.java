package emanondev.minigames.generic;

public interface Registrable {

    boolean isRegistered();

    /**
     * internal use only
     *
     * @param id
     */
    void setRegistered(String id);

    String getId();

    /**
     * internal use only
     */
    void setUnregister();

}
