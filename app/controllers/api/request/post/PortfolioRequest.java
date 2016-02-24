package controllers.api.request.post;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModelProperty;

import dao.pmo.ActorDao;
import dao.pmo.PortfolioDao;
import models.framework_models.parent.IModelConstants;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

/**
 * The Portfolio post/put request.
 * 
 * @author Marc Schaer
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioRequest {

    @JsonProperty
    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    @ApiModelProperty(required = true)
    @Required
    public String name;

    @JsonProperty
    @ApiModelProperty(required = true)
    public Boolean isActive;

    @JsonProperty
    @ApiModelProperty(required = true)
    @Required
    public Long managerId;

    @JsonProperty
    @ApiModelProperty(required = true)
    @Required
    public Long portfolioTypeId;

    @JsonProperty
    @Column(length = IModelConstants.MEDIUM_STRING)
    @ApiModelProperty(required = false)
    @Required
    public String refId;

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (managerId != null && ActorDao.getActorById(managerId) == null) {
            errors.add(new ValidationError("managerId", "The manager does not exist"));
        }
        if (portfolioTypeId != null && PortfolioDao.getPortfolioTypeById(portfolioTypeId) == null) {
            errors.add(new ValidationError("portfolioTypeId", "The portfolio type does not exist"));
        }

        return errors.isEmpty() ? null : errors;

    }
}
