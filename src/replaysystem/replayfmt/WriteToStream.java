package replaysystem.replayfmt;

import java.io.DataOutputStream;
import java.io.IOException;

public interface WriteToStream {
    void writeToStream(DataOutputStream stream) throws IOException;
}
