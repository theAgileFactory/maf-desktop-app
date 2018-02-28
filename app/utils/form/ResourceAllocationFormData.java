package utils.form;

import framework.utils.Utilities;
import models.common.ResourceAllocation;
import models.common.ResourceAllocationDetail;
import models.finance.PortfolioEntryResourcePlanAllocationStatusType;
import play.Logger;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.i18n.Messages;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ResourceAllocationFormData {

    // the portfolioEntry id
    public Long id;

    public Long allocationId;

    public Long portfolioEntryPlanningPackage;

    public String allocationStatus;

    public Long lastStatusTypeUpdateActor = 0L;

    public String lastStatusTypeUpdateTime;

    public boolean followPackageDates;

    @Constraints.Required
    public BigDecimal days;

    public String startDate;

    public String endDate;

    public boolean monthlyAllocated;

    public List<MonthAllocation> monthAllocations;

    public static class MonthAllocation {

        public Integer year;
        public Double januaryAllocationValue;
        public Double februaryAllocationValue;
        public Double marchAllocationValue;
        public Double aprilAllocationValue;
        public Double mayAllocationValue;
        public Double juneAllocationValue;
        public Double julyAllocationValue;
        public Double augustAllocationValue;
        public Double septemberAllocationValue;
        public Double octoberAllocationValue;
        public Double novemberAllocationValue;
        public Double decemberAllocationValue;

        public MonthAllocation() {
        }

        public MonthAllocation(Integer year) {
            this.year = year;
        }

        public void addValue(Integer month, Double days) {
            switch (month) {
                case 0:
                    this.januaryAllocationValue = days;
                    break;
                case 1:
                    this.februaryAllocationValue = days;
                    break;
                case 2:
                    this.marchAllocationValue = days;
                    break;
                case 3:
                    this.aprilAllocationValue = days;
                    break;
                case 4:
                    this.mayAllocationValue = days;
                    break;
                case 5:
                    this.juneAllocationValue = days;
                    break;
                case 6:
                    this.julyAllocationValue = days;
                    break;
                case 7:
                    this.augustAllocationValue = days;
                    break;
                case 8:
                    this.septemberAllocationValue = days;
                    break;
                case 9:
                    this.octoberAllocationValue = days;
                    break;
                case 10:
                    this.novemberAllocationValue = days;
                    break;
                case 11:
                default:
                    this.decemberAllocationValue = days;
            }
        }
    }

    public ResourceAllocationFormData() {
        this.monthAllocations = new ArrayList<>();
        this.allocationStatus = PortfolioEntryResourcePlanAllocationStatusType.AllocationStatus.DRAFT.name();
    }

    /**
     * Validate the dates.
     */
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        if (this.startDate != null && this.endDate != null) {

            try {

                if (!this.startDate.equals("") && this.endDate.equals("")) {
                    // the start date cannot be filled alone
                    errors.add(new ValidationError("startDate", Messages.get("object.allocated_resource.start_date.invalid")));
                }

                Date startDateAsDate = Utilities.getDateFormat(null).parse(this.startDate);
                Date endDateAsDate = Utilities.getDateFormat(null).parse(this.endDate);

                if (!this.startDate.equals("") && !this.endDate.equals("")
                        && startDateAsDate.after(endDateAsDate)) {
                    // the end date should be after the start date
                    errors.add(new ValidationError("endDate", Messages.get("object.allocated_resource.end_date.invalid")));
                }

                Calendar startCal = Calendar.getInstance();
                Calendar endCal = Calendar.getInstance();
                startCal.setTime(startDateAsDate);
                endCal.setTime(endDateAsDate);

                int duration = Utilities.getDuration(startDateAsDate, endDateAsDate);

                if (    startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && duration == 2
                    ||  startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && duration == 1) {
                    errors.add(new ValidationError("endDate", Messages.get("object.allocated_resource.end_date.invalid")));
                }

            } catch (Exception e) {
                Logger.warn("impossible to parse the allocation dates when testing the formats");
            }
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * Process an allocation detail.
     * If a positive number of days is provided, the corresponding allocation detail will be created or updated and returned.
     * Else, the allocation detail will be deleted and null will be returned.
     *
     * @param allocation the parent resource allocation
     * @param year the year of the allocation
     * @param month the month of the allocation
     * @param days the number of allocated days for the resource
     *
     * @return the created/updated allocation or null in case of deletion
     */
    protected ResourceAllocationDetail processMonthAllocation(ResourceAllocation allocation, Integer year, Integer month, Double days) {
        if (days != null && days != 0) {
            return allocation.createOrUpdateAllocationDetail(year, month, days);
        } else {
            ResourceAllocationDetail detail = allocation.getDetail(year, month);
            if (detail != null) {
                allocation.getDetails().remove(detail);
                detail.doDelete();
            }
            return null;
        }
    }
}
