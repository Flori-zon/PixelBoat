import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FMLBuilder {

    // "fuck my life"

    static List<View> build(File fmlFile) {
        StringBuilder fmlBuilder = new StringBuilder();
        try {
            Scanner sc = new Scanner(fmlFile);
            String line;
            while ((line = sc.nextLine()) != null)
                fmlBuilder.append(line).append(" ");
        } catch (Exception ignored) {
        }
        return build(fmlBuilder.toString());
    }

    static List<View> build(String fmlString) {
        List<String> args = new ArrayList<>();
        for (String arg : fmlString.split(" "))
            if (arg.length() != 0) args.add(arg);
        return build(args);
    }

    static private List<View> build(List<String> args) {
        List<View> views = new ArrayList<>();
        while (next(args, "+")) {
            String type = args.remove(0); // TODO
            Data data = new Data();
            List<View> children = new ArrayList<>();
            while (args.size() != 0) {
                if (next(args, "(")) {
                    List<String> a = new ArrayList<>();
                    while (!next(args, ")"))
                        a.add(args.get(0));
                    children.addAll(build(a));
                } else
                    data.set(args.remove(0));
            }
            views.add(new View(data, children));
        }
        return views;
    }

    static private boolean next(List<String> args, String s) {
        if (args.get(0).equals(s)) {
            args.remove(0);
            return true;
        }
        return false;
    }

}
