package services.datasyndication.models;

import framework.utils.Msg;

/**
 * Define an item for a data type that could by synchronized (for data
 * syndication).
 * 
 * @author Johann Kohler
 *
 */
public class DataSyndicationAgreementItem {

    public Long id;
    public String dataType;
    public String descriptor;

    @Override
    public String toString() {
        return this.getFullLabel();
    }

    /**
     * Get the label of the item.
     */
    public String getLabel() {
        return Msg.get("object.data_syndication_agreement_item." + this.dataType + "." + this.descriptor + ".label");
    }

    /**
     * Get the full label of the item.
     */
    public String getFullLabel() {
        return Msg.get("object.data_syndication_agreement_item." + this.dataType + ".label") + " - "
                + Msg.get("object.data_syndication_agreement_item." + this.dataType + "." + this.descriptor + ".label");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DataSyndicationAgreementItem) {
            DataSyndicationAgreementItem toCompare = (DataSyndicationAgreementItem) o;
            return this.id.equals(toCompare.id);
        }
        return false;
    }

}
