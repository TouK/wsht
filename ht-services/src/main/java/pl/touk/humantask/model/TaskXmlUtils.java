package pl.touk.humantask.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

class TaskXmlUtils {
    
    private final Log log = LogFactory.getLog(TaskXmlUtils.class);

    private XPathFactory xPathFactory = null;
    
    private NamespaceContext namespaceContext;
    
    private Map<String, Message> input;
    private Map<String, Message> output;

    public TaskXmlUtils(NamespaceContext namespaceContext, Map<String, Message> input, Map<String, Message> output) {
        super();
        this.xPathFactory = XPathFactory.newInstance();
        this.namespaceContext = namespaceContext;
        this.input = input;
        this.output = output;
    }
    
    /**
     * Creates {@link XPath} aware of request namespaces.
     */
    synchronized XPath createXPathInstance() {
        
        XPath xpath = this.xPathFactory.newXPath();

        xpath.setNamespaceContext(this.namespaceContext);
        xpath.setXPathFunctionResolver(new XPathFunctionResolver() {

            public XPathFunction resolveFunction(QName functionName, int arity) {
    
                if (functionName == null) {
                    throw new NullPointerException("The function name cannot be null.");
                }
    
                if (functionName.equals(new QName("http://www.example.org/WS-HT", "getInput", "htd"))) {
    
                    return new GetInputXPathFunction();
                }
                
                if (functionName.equals(new QName("http://www.example.org/WS-HT", "getOutput", "htd"))) {
    
                    return new GetOutputXPathFunction();
                } 
                    
                return null;
            }
        });

        return xpath;
    }
    
    /**
     * Evaluates XPath expression in context of the Task. Expression can contain 
     * XPath Extension functions as defined by WS-HumanTask v1. Following
     * XPath functions are implemented:
     * <ul>
     * <li> {@link GetInputXPathFunction} </li>
     * <li> {@link GetOutputXPathFunction} </li>
     * </ul>
     * @param xPathString The XPath 1.0 expression.
     * @param returnType The desired return type. See {@link XPathConstants}.
     * @return The result of evaluating the <code>XPath</code> function as an <code>Object</code>.
     */
    public Object evaluateXPath(String xPathString, QName returnType) {
        
        Validate.notNull(xPathString);
        Validate.notNull(returnType);

        Object o = null;

        XPath xpath = createXPathInstance();

        try {

            //TODO create empty document only once
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document emptyDocument = builder.newDocument();

            XPathExpression expr = xpath.compile(xPathString);
            o = expr.evaluate(emptyDocument, returnType);
               
        } catch (XPathExpressionException e) {
            
            log.error("Error evaluating XPath: " + xPathString, e);

        } catch (ParserConfigurationException e) {
            
            log.error("Error evaluating XPath:  " + xPathString, e);
        }

        return o;    
    }
    
    /**
     * Implements getInput {@link XPathFunction} - get the data for the part of the task's input message.
     * @author Witek Wołejszo
     */
    private class GetInputXPathFunction implements XPathFunction {
        
        private final Log log = LogFactory.getLog(GetInputXPathFunction.class);

        /**
         * <p>Evaluate the function with the specified arguments.</p>
         * @see XPathFunction#evaluate(List)
         * @param args The arguments, <code>null</code> is a valid value.
         * @return The result of evaluating the <code>XPath</code> function as an <code>Object</code>.
         * @throws XPathFunctionException If <code>args</code> cannot be evaluated with this <code>XPath</code> function.
         */
        public Object evaluate(List args) throws XPathFunctionException {

            String partName = (String) args.get(0);

            Message message = input.get(partName);
            Document document = null;
            
            if (message == null) {
                throw new XPathFunctionException("Task's input does not contain partName: " + args.get(0));
            }

            try {
                
                document = message.getDomDocument();
                
            } catch (ParserConfigurationException e) {

                throw new XPathFunctionException(e);
            } catch (SAXException e) {
                
                throw new XPathFunctionException(e);
            } catch (IOException e) {
                
                throw new XPathFunctionException(e);
            }
            
            return document == null ? null : document.getElementsByTagName(partName);
        }

    }
    
    /**
     * Implements getOutput {@link XPathFunction} - get the data for the part of the task's output message.
     * @author Witek Wołejszo
     */
    private class GetOutputXPathFunction implements XPathFunction {
        
        private final Log log = LogFactory.getLog(GetOutputXPathFunction.class);

        /**
         * <p>Evaluate the function with the specified arguments.</p>
         * @see XPathFunction#evaluate(List)
         * @param args The arguments, <code>null</code> is a valid value.
         * @return The result of evaluating the <code>XPath</code> function as an <code>Object</code>.
         * @throws XPathFunctionException If <code>args</code> cannot be evaluated with this <code>XPath</code> function.
         */
        public Object evaluate(List args) throws XPathFunctionException {

            String partName = (String) args.get(0);
            
            Message message = output.get(partName);
            Document document = null;
            
            if (message == null) {
                throw new XPathFunctionException("Task's output does not contain partName: " + args.get(0));
            }

            try {
                
                document = message.getDomDocument();
                
            } catch (ParserConfigurationException e) {

                throw new XPathFunctionException(e);
            } catch (SAXException e) {
                
                throw new XPathFunctionException(e);
            } catch (IOException e) {
                
                throw new XPathFunctionException(e);
            }
            
            return document == null ? null : document.getElementsByTagName(partName);
        }

    }

}
