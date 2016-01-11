package framework.services.widgets;

/**
 * An enumeration which is listing all the possible row templates.<br/>
 * A page is made of the combination of multiple rows.<br/>
 * Each row template is associated with a layout which consist in a comma separated list
 * of integers. Each integer is a number matching the Bootstrap grid system.
 * @author Pierre-Yves Cloux
 */
public enum DashboardRowTemplate{
    TPL12COL_1("12"),
    TPL66COL_2("6,6"),
    TPL444COL_3("4,4,4"),
    TPL48_OL_2("4,8"),
    TPL84COL_2("8,4");
    
    private String layout;
    
    public String getLayout() {
        return layout;
    }

    private DashboardRowTemplate(String layout) {
        this.layout = layout;
    }
}