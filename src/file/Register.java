package file;

import java.io.IOException;

public interface Register {
    public void setId(int i);
    public int getId();
    public byte[] toByteArray() throws IOException;
    public void fromByteArray(byte[] b) throws IOException;
}