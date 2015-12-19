package martinmatko.anatomy;

/**
 * Created by Martin on 19.12.2015.
 */
public class Term {
    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Term(String name, String identifier) {

        this.name = name;
        this.identifier = identifier;
    }

    public String identifier;
}
