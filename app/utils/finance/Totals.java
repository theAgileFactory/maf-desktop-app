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
package utils.finance;

/**
 * A totals instance allows to manager the budget, cost to complete and engaged
 * totals.
 * 
 * @author Johann Kohler
 * 
 */
public class Totals {

    private Double opexBudget = 0.0;
    private Double capexBudget = 0.0;
    private Double opexCostToComplete = 0.0;
    private Double capexCostToComplete = 0.0;
    private Double opexEngaged = 0.0;
    private Double capexEngaged = 0.0;

    /**
     * Default constructor.
     * 
     * @param opexBudget
     *            the opex budget amount
     * @param capexBudget
     *            the capex budget amount
     * @param opexCostToComplete
     *            the opex cost to complete amount
     * @param capexCostToComplete
     *            the capex cost to complete amount
     * @param opexEngaged
     *            the opex engaged amount
     * @param capexEngaged
     *            the capex engaged amount
     */
    public Totals(Double opexBudget, Double capexBudget, Double opexCostToComplete, Double capexCostToComplete, Double opexEngaged, Double capexEngaged) {
        this.setOpexBudget(opexBudget);
        this.setCapexBudget(capexBudget);
        this.setOpexCostToComplete(opexCostToComplete);
        this.setCapexCostToComplete(capexCostToComplete);
        this.setOpexEngaged(opexEngaged);
        this.setCapexEngaged(capexEngaged);
    }

    /**
     * Get the OPEX budget.
     */
    public Double getOpexBudget() {
        return opexBudget;
    }

    /**
     * Set the OPEX budget.
     * 
     * @param opexBudget
     *            the OPEX budget
     */
    public void setOpexBudget(Double opexBudget) {
        this.opexBudget = opexBudget;
    }

    /**
     * Get the CAPEX budget.
     */
    public Double getCapexBudget() {
        return capexBudget;
    }

    /**
     * Set the CAPEX budget.
     * 
     * @param capexBudget
     *            the CAPEX budget
     */
    public void setCapexBudget(Double capexBudget) {
        this.capexBudget = capexBudget;
    }

    /**
     * Get the OPEX cost to complete.
     */
    public Double getOpexCostToComplete() {
        return opexCostToComplete;
    }

    /**
     * Set the OPEX cost to complete.
     * 
     * @param opexCostToComplete
     *            the OPEX cost to complete
     */
    public void setOpexCostToComplete(Double opexCostToComplete) {
        this.opexCostToComplete = opexCostToComplete;
    }

    /**
     * Get the CAPEX cost to complete.
     */
    public Double getCapexCostToComplete() {
        return capexCostToComplete;
    }

    /**
     * Set the CAPEX cost to complete.
     * 
     * @param capexCostToComplete
     *            the CAPEX cost to complete
     */
    public void setCapexCostToComplete(Double capexCostToComplete) {
        this.capexCostToComplete = capexCostToComplete;
    }

    /**
     * Get the OPEX engaged amount.
     */
    public Double getOpexEngaged() {
        return opexEngaged;
    }

    /**
     * Set the OPEX engaged amount.
     * 
     * @param opexEngaged
     *            the OPEX engaged amount
     */
    public void setOpexEngaged(Double opexEngaged) {
        this.opexEngaged = opexEngaged;
    }

    /**
     * Get the CAPEX engaged amount.
     */
    public Double getCapexEngaged() {
        return capexEngaged;
    }

    /**
     * Set the CAPEX engaged amount.
     * 
     * @param capexEngaged
     *            the CAPEX engaged amount
     */
    public void setCapexEngaged(Double capexEngaged) {
        this.capexEngaged = capexEngaged;
    }

    /**
     * Get the forecast.
     * 
     * @param isOpex
     *            set to true if OPEX, else CAPEX
     */
    public Double getForecast(boolean isOpex) {
        if (isOpex) {
            return opexCostToComplete + opexEngaged;
        } else {
            return capexCostToComplete + capexEngaged;
        }

    }

    /**
     * Get the opex deviation.<br/>
     * (costToComplete + engaged) - budget
     * 
     * @param isOpex
     *            set to true if OPEX, else CAPEX
     */
    public Double getDeviation(boolean isOpex) {
        if (isOpex) {
            return opexCostToComplete + opexEngaged - opexBudget;
        } else {
            return capexCostToComplete + capexEngaged - capexBudget;
        }
    }

    /**
     * Get the deviation rate in percent.<br/>
     * (deviation / budget) * 100
     * 
     * @param isOpex
     *            set to true if OPEX, else CAPEX
     */
    public Double getDeviationRate(boolean isOpex) {

        Double budget = isOpex ? getOpexBudget() : getCapexBudget();

        if (budget.equals(Double.valueOf(0))) {
            return null;
        } else {
            Double deviation = getDeviation(isOpex);
            return (deviation / budget) * 100;
        }
    }

}
