package da.gammla.AnchoredTable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Mega
 * Intellij IDEA
 */
public class AnchoredTable implements Serializable {

    ArrayList<AnchoredData> data_table;
    String table_name = "None";

    public AnchoredTable(String table_name){
        this.table_name = table_name;
        data_table = new ArrayList<AnchoredData>();
    }

    public AnchoredTable(File file) throws Exception {
        data_table = new ArrayList<AnchoredData>();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(file);
        Element doc_element = doc.getDocumentElement();
        doc_element.normalize();

        table_name = doc_element.getNodeName();

        NodeList datas = doc_element.getChildNodes();

        for (int i = 0; i < datas.getLength(); i++) {
            if (datas.item(i).hasAttributes()) {
                data_table.add(new AnchoredData(datas.item(i).getAttributes().getNamedItem("Anchor").getNodeValue(),
                        datas.item(i).getAttributes().getNamedItem("Data").getNodeValue()
                        ));
            }
        }
    }


    public void setData(String anchor, String data){
        for (AnchoredData tbldata: data_table)
            if (tbldata.isQualified(anchor)){
                tbldata.setData(data);
                return;
            }
        data_table.add(new AnchoredData(anchor, data));
    }

    public String getData(String anchor){
        for (AnchoredData tbldata: data_table)
            if (tbldata.isQualified(anchor)){
                return tbldata.getData();
            }
        return null;
    }

    public void setDataAt(int index, String data) {
        if(data_table.get(index) != null){
            data_table.get(index).setData(data);
        }
    }

    public void setAnchorAt(int index, String anchor) {
        if(data_table.get(index) != null){
            data_table.get(index).setAnchor(anchor);
        }
    }

    public String getDataAt(int index) {
        if(data_table.get(index) != null){
            return data_table.get(index).getData();
        }
        return null;
    }

    public String getAnchorAt(int index) {
        if(data_table.get(index) != null){
            return data_table.get(index).getAnchor();
        }
        return null;
    }

    public int getAnchorPosition(String anchor){
        for (int i = 0; i < data_table.size(); i++) {
            if (data_table.get(i).isQualified(anchor))
                return i;
        }
        return -1;
    }

    public AnchoredData getAnchoredData(String anchor){
        return data_table.get(getAnchorPosition(anchor));
    }

    public int getSize(){
        return data_table.size();
    }

    public boolean isEmpty(){
        return data_table.isEmpty();
    }

    public void removeData(String anchor){
        for (int i = 0; i < getSize(); i++) {
            if (data_table.get(i).isQualified(anchor)){
                data_table.remove(i);
            }
        }
    }

    public void removeDataAt(int index){
        data_table.remove(index);
    }

    public boolean hasData(String anchor){
        for (AnchoredData data: data_table)
            if (data.isQualified(anchor))
                return true;
        return false;
    }

    public void switchAnchors(String anchor1, String anchor2){
        AnchoredData saved_anchored_data = getAnchoredData(anchor2);
        int saved_position = getAnchorPosition(anchor1);
        data_table.set(getAnchorPosition(anchor2), getAnchoredData(anchor1));
        data_table.set(saved_position, saved_anchored_data);
    }

    public String asString(){
        StringBuilder sb = new StringBuilder();
        sb.append(table_name);
        sb.append(" : {");
        for (AnchoredData data:data_table) {
            sb.append('[');
            sb.append(data.anchor);
            sb.append(',');
            sb.append(data.data);
            sb.append("],");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append('}');
        return sb.toString();
    }

    public boolean saveToXML(String directory){
        try {

            File file = new File(directory);
            file.getParentFile().mkdirs();
            file.createNewFile();


            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(table_name);
            doc.appendChild(rootElement);


            for (AnchoredData anchored_data:data_table) {
                Element anchored_data_element = doc.createElement("AnchoredData");
                rootElement.appendChild(anchored_data_element);

                anchored_data_element.setAttribute("Anchor", anchored_data.getAnchor());
                anchored_data_element.setAttribute("Data", anchored_data.getData());

            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);
            return true;
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            return false;
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
