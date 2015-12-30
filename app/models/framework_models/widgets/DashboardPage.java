package models.framework_models.widgets;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.avaje.ebean.Model;

import models.framework_models.parent.IModelConstants;

/**
 * An object which is storing the configuration for a dashboard page.<br/>
 * A dashboard page has:
 * <ul>
 * <li>a name which is displayed to the end user (as the title of the page)</li>
 * <li>a template which is identifying a template type (= how the widgets are distributed on the page)</li>
 * </ul>
 * @author Pierre-Yves Cloux
 */
@Entity
public class DashboardPage  extends Model {    
    @Id
    public Long id;
    
    @Column(length = IModelConstants.LARGE_STRING, nullable = false)
    public String name;

    @Column(length = IModelConstants.SMALL_STRING, nullable = false)
    public String template;
    
    @OneToMany(mappedBy = "dashboardPage", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    public List<DashboardWidget> dashboardWidgets;

    public DashboardPage() {
    }
}
