package models.framework_models.widgets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Model;

import models.framework_models.parent.IModelConstants;

/**
 * An object which is storing the configuration for a dashboard widget.
 * <ul>
 * <li>identifier : the unique identifier for the widget application</li>
 * <li>order : the order of the widget within its "template" column</li>
 * <li>config : the configuration for the widget (serialized)</li>
 * </ul>
 * @author Pierre-Yves Cloux
 */
@Entity
public class DashboardWidget  extends Model {    
    public DashboardWidget() {
    }

    @Id
    public Long id;
    
    @Column(length = IModelConstants.MEDIUM_STRING, nullable = false)
    public String identifier;
    
    @Column(length = IModelConstants.SMALL_STRING, nullable = false)
    public String position;
    
    @Column(name = "`order`")
    public int order;
    
    @Lob
    public byte[] config;
    
    @ManyToOne(optional = false)
    public DashboardPage dashboardPage;
}
