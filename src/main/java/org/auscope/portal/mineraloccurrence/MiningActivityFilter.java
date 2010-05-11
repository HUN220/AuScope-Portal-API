package org.auscope.portal.mineraloccurrence;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Mathew Wyatt
 * 
 * @version $Id$
 */
public class MiningActivityFilter implements IFilter {
    private List<Mine> associatedMines;
    private String startDate;
    private String endDate;
    private String oreProcessed;
    private String producedMaterial;
    private String cutOffGrade;
    private String production;
    private boolean manyParameters;
    
    // -------------------------------------------------------------- Constants
    
    /** Log object for this class. */
    protected final Log log = LogFactory.getLog(getClass());
    
    
    // ----------------------------------------------------------- Constructors
    
    public MiningActivityFilter(List<Mine> associatedMines,
                                String startDate,
                                String endDate,
                                String oreProcessed,
                                String producedMaterial,
                                String cutOffGrade,
                                String production) {
        this.associatedMines = associatedMines;
        this.startDate = startDate;
        this.endDate = endDate;
        this.oreProcessed = oreProcessed;
        this.producedMaterial = producedMaterial;
        this.cutOffGrade = cutOffGrade;
        this.production = production;
        this.manyParameters = getParameterIndicator();
    }

    // --------------------------------------------------------- Public Methods
    
    /**
     * Build the query string based on given properties
     * @return the query string
     */
    public String getFilterString() { // TODO: this sucks! use geotools api to build queries...
        
        String result = "";
        StringBuffer filterClause = new StringBuffer();
        StringBuffer filterExpression  = new StringBuffer();
        

        filterClause.append("<ogc:Filter xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\"\n");
        filterClause.append("            xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\"\n");
        filterClause.append("            xmlns:ogc=\"http://www.opengis.net/ogc\"\n");
        filterClause.append("            xmlns:gml=\"http://www.opengis.net/gml\"\n");
        filterClause.append("            xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n");
        filterClause.append("            xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");

        if (manyParameters)
            filterExpression.append("  <ogc:And>\n");

        int associatedMinesCount = this.associatedMines.size();
        log.debug("Number of associated mines: " + associatedMinesCount);
        
        // One mine, we don't need ogc:Or's
        if (associatedMinesCount == 1 ) {

            int relatedActivitiesCount = this.associatedMines.get(0).getRelatedActivities().size();
            log.debug("___Number of related activities: " + relatedActivitiesCount);
            
            if (relatedActivitiesCount == 1 ) {
                filterExpression.append("    <ogc:PropertyIsEqualTo>\n" +
                                        "      <ogc:PropertyName>gml:name</ogc:PropertyName>\n" +
                                        "      <ogc:Literal>" + this.associatedMines.get(0).getRelatedActivities().get(0) + "</ogc:Literal>\n" +
                                        "    </ogc:PropertyIsEqualTo>\n");                
            }
            // One mine but more then one mining activity, need ogc:Or's
            else if (relatedActivitiesCount > 1 ) {
                filterExpression.append("  <ogc:Or>\n");
                
                for (String s : this.associatedMines.get(0).getRelatedActivities()) {

                    filterExpression.append("    <ogc:PropertyIsEqualTo>\n" +
                                            "      <ogc:PropertyName>gml:name</ogc:PropertyName>\n" +
                                            "      <ogc:Literal>" + s + "</ogc:Literal>\n" +
                                            "    </ogc:PropertyIsEqualTo>\n");
                    
                }               
                filterExpression.append("  </ogc:Or>\n");
            }
            
        // For two and more mines we need ogc:Or's
        } else if (associatedMinesCount > 1) {
            
            filterExpression.append("  <ogc:Or>\n");
            int z = 0;
            for (Mine mine : this.associatedMines) {

                log.debug((++z) + " : " + mine.getMineNameURI());

                int relActCount = mine.getRelatedActivities().size();
                log.debug("___Number of mine related activities: " + relActCount);

                int y = 0;
                for (String s : mine.getRelatedActivities()) {
                    log.debug("___" + (++y) + " : " + s);
                    
                    filterExpression.append("    <ogc:PropertyIsEqualTo>\n" +
                                            "      <ogc:PropertyName>gml:name</ogc:PropertyName>\n" +
                                            "      <ogc:Literal>" + s + "</ogc:Literal>\n" +
                                            "    </ogc:PropertyIsEqualTo>\n");
                }
            }
            
            filterExpression.append("  </ogc:Or>\n");            
        }

        if(!this.startDate.equals(""))
            filterExpression.append("    <ogc:PropertyIsGreaterThan>\n" +
                                    "      <ogc:PropertyName>er:activityDuration/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition</ogc:PropertyName>\n" +
                                    "      <ogc:Literal>"+ this.startDate +"</ogc:Literal>\n" +
                                    "    </ogc:PropertyIsGreaterThan>\n");
        if(!this.endDate.equals(""))
            filterExpression.append("    <ogc:PropertyIsLessThan>\n" +
                                    "      <ogc:PropertyName>er:activityDuration/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition</ogc:PropertyName>\n" +
                                    "      <ogc:Literal>"+this.endDate+"</ogc:Literal>\n" +
                                    "    </ogc:PropertyIsLessThan>\n");
        if(!this.oreProcessed.equals(""))
            filterExpression.append("    <ogc:PropertyIsGreaterThan>\n" +
                                    "      <ogc:PropertyName>er:oreProcessed/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                                    "      <ogc:Literal>"+this.oreProcessed+"</ogc:Literal>\n" +
                                    "    </ogc:PropertyIsGreaterThan>");            
        if(!this.producedMaterial.equals(""))
            filterExpression.append("    <ogc:PropertyIsEqualTo>\n" +
                                    "      <ogc:PropertyName>er:producedMaterial/er:Product/er:productName/gsml:CGI_TermValue/gsml:value</ogc:PropertyName>\n" +
                                    "      <ogc:Literal>"+this.producedMaterial+"</ogc:Literal>\n" +
                                    "    </ogc:PropertyIsEqualTo>\n");
        if(!this.cutOffGrade.equals(""))
            filterExpression.append("    <ogc:PropertyIsGreaterThan>\n" +
                                    "      <ogc:PropertyName>er:producedMaterial/er:Product/er:grade/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                                    "      <ogc:Literal>"+this.cutOffGrade+"</ogc:Literal>\n" +
                                    "    </ogc:PropertyIsGreaterThan>");
        if(!this.production.equals(""))
            filterExpression.append("    <ogc:PropertyIsGreaterThan>\n" +
                                    "      <ogc:PropertyName>er:producedMaterial/er:Product/er:production/gsml:CGI_NumericValue/gsml:principalValue</ogc:PropertyName>\n" +
                                    "      <ogc:Literal>"+this.production+"</ogc:Literal>\n" +
                                    "    </ogc:PropertyIsGreaterThan>\n");
        if(manyParameters)
            filterExpression.append("  </ogc:And>\n");

        // If there are no query parameters and the query sting is empty, we are 
        // returning an empty string. In this case GetFeature request will be
        // sent without ogc:Filter clause
        if (filterExpression.length() != 0)  {
            filterExpression.append("</ogc:Filter>");
            result = filterClause.append(filterExpression).toString();
        }
        
        return result;
    }

    
    // -------------------------------------------------------- Private Methods
    
    /*
     * Checks if more than one query parameter have a value.   
     * @return <tt>true</tt> if more than one parameter is found
     */
    private boolean getParameterIndicator() {
        int howManyHaveaValue = 0;

        if(this.associatedMines.size() >= 1)
            howManyHaveaValue++;
        if(!this.startDate.equals(""))
            howManyHaveaValue++;
        if(!this.endDate.equals(""))
            howManyHaveaValue++;
        if(!this.oreProcessed.equals(""))
            howManyHaveaValue++;
        if(!this.producedMaterial.equals(""))
            howManyHaveaValue++;
        if(!this.cutOffGrade.equals(""))
            howManyHaveaValue++;
        if(!this.production.equals(""))
            howManyHaveaValue++;

        if(howManyHaveaValue >= 2)
            return true;

        return false;
    }

}
