/*! LICENSE
 *
 * Copyright (c) 2015, The Agile Factory SA and/or its affiliates. All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package controllers.api.request.post;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModelProperty;

import dao.finance.CostCenterDAO;
import dao.finance.CurrencyDAO;
import dao.finance.PurchaseOrderDAO;
import dao.finance.SupplierDAO;
import dao.pmo.ActorDao;
import framework.services.api.commons.IApiConstants;
import models.finance.WorkOrder;
import models.framework_models.parent.IModelConstants;
import play.data.format.Formats.DateTime;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;

/**
 * The purchase order line item post/put request.
 * 
 * @author Marc Schaer
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PurchaseOrderLineItemRequest {

    @JsonProperty
    @ApiModelProperty(required = true)
    @Required
    public Boolean isCancelled;

    @MaxLength(value = IModelConstants.MEDIUM_STRING)
    @JsonProperty
    @ApiModelProperty(required = true)
    public String refId;

    @MaxLength(value = IModelConstants.VLARGE_STRING)
    @JsonProperty
    @ApiModelProperty(required = true)
    public String description;

    @JsonProperty
    @ApiModelProperty(required = true)
    public Long currencyId;

    @JsonProperty
    public BigDecimal currencyRate;

    @JsonProperty
    @ApiModelProperty(required = true)
    @Required
    public Integer lineId;

    @JsonProperty
    public Long supplierId;

    @JsonProperty
    public BigDecimal quantity;

    @JsonProperty
    public BigDecimal quantityTotalReceived;

    @JsonProperty
    public BigDecimal quantityBilled;

    @JsonProperty
    @ApiModelProperty(required = true)
    public BigDecimal amount;

    @JsonProperty
    public BigDecimal amountReceived;

    @JsonProperty
    public BigDecimal amountBilled;

    @JsonProperty
    public BigDecimal unitPrice;

    @MaxLength(value = IModelConstants.SMALL_STRING)
    @JsonProperty
    public String materialCode;

    @MaxLength(value = IModelConstants.SMALL_STRING)
    @JsonProperty
    public String glAccount;

    @JsonProperty
    @ApiModelProperty(required = true)
    public Boolean isOpex;

    @DateTime(pattern = IApiConstants.DATE_FORMAT)
    @JsonProperty
    @ApiModelProperty(required = true)
    public Date creationDate;

    @DateTime(pattern = IApiConstants.DATE_FORMAT)
    @JsonProperty
    public Date dueDate;

    // FIXME
    @JsonProperty
    public Long shipmentTypeId;

    @JsonProperty
    public Long requesterId;

    @JsonProperty
    public Long costCenterId;

    // TODO : create API for goods receipt
    /*
     * @JsonProperty public List<GoodsReceipt> goodsReceipts;
     */

    @JsonProperty
    @ApiModelProperty(required = true)
    @Required
    public Long purchaseOrderId;

    // FIXME
    @JsonProperty
    public List<WorkOrder> workOrders;

    /**
     * Form validator.
     */
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (currencyId != null && CurrencyDAO.getCurrencyById(currencyId) == null) {
            errors.add(new ValidationError("currencyId", "The currencyId does not exist"));
        }
        if (supplierId != null && SupplierDAO.getSupplierById(supplierId) == null) {
            errors.add(new ValidationError("supplierId", "The supplierId does not exist"));
        }
        if (costCenterId != null && CostCenterDAO.getCostCenterById(costCenterId) == null) {
            errors.add(new ValidationError("costCenterId", "The costCenterId does not exist"));
        }
        if (requesterId != null && ActorDao.getActorById(requesterId) == null) {
            errors.add(new ValidationError("requesterId", "The requesterId does not exist"));
        }
        if (shipmentTypeId != null && PurchaseOrderDAO.getPurchaseOrderLineShipmentStatutsTypeById(shipmentTypeId) == null) {
            errors.add(new ValidationError("shipmentTypeId", "The shipmentTypeId does not exist"));
        }
        // TODO
        return errors.isEmpty() ? null : errors;

    }

}
