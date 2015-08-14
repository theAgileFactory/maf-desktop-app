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
        return Msg.get("object.data_syndication_agreement_item." + this.dataType + "." + this.descriptor + ".label");
    }

}
