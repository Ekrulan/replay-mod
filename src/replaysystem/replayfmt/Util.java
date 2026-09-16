package replaysystem.replayfmt;

import arc.files.Fi;

public class Util {

    public static Fi dotIndex(Fi fi) {
        return fi.sibling(fi.name() + ".index");
    }
}
