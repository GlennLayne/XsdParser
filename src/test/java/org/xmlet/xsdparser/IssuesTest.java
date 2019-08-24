package org.xmlet.xsdparser;

import org.junit.Assert;
import org.junit.Test;
import org.xmlet.xsdparser.core.XsdParser;
import org.xmlet.xsdparser.xsdelements.*;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IssuesTest {

    private static final List<XsdElement> elements;
    private static final List<XsdSchema> schemas;

    private static final XsdParser parser;

    static {
        parser = new XsdParser(getFilePath());

        schemas = parser.getResultXsdSchemas().collect(Collectors.toList());
        elements = parser.getResultXsdElements().collect(Collectors.toList());
    }

    @Test
    public void testSubstitutionGroup(){
        List<XsdElement> elements = parser.getResultXsdElements().collect(Collectors.toList());

        Optional<XsdElement> navnOptional = elements.stream().filter(element -> element.getName().equals("navn")).findFirst();
        Optional<XsdElement> kundeOptional = elements.stream().filter(element -> element.getName().equals("kunde")).findFirst();

        Assert.assertTrue(navnOptional.isPresent());
        Assert.assertTrue(kundeOptional.isPresent());

        XsdElement navnElement = navnOptional.get().getXsdSubstitutionGroup();
        XsdElement kundeElement = kundeOptional.get().getXsdSubstitutionGroup();

        Assert.assertEquals("xsd:string", navnElement.getType());

        XsdComplexType kundeComplexType = kundeElement.getXsdComplexType();

        Assert.assertNotNull(kundeComplexType);
        Assert.assertEquals("custinfo", kundeComplexType.getName());

        XsdSequence custinfoSequence = kundeComplexType.getChildAsSequence();

        Assert.assertNotNull(custinfoSequence);
        List<XsdElement> sequenceElements = custinfoSequence.getChildrenElements().collect(Collectors.toList());

        Assert.assertNotNull(sequenceElements);
        Assert.assertEquals(1, sequenceElements.size());

        XsdElement nameElement = sequenceElements.get(0);

        Assert.assertEquals(navnElement.getName(), nameElement.getName());
        Assert.assertEquals(navnElement.getType(), nameElement.getType());
    }

    @Test
    public void testDocumentationWithCDATA(){
        Optional<XsdElement> someElementOpt = elements.stream().filter(e -> e.getName().equals("someElement")).findFirst();

        Assert.assertTrue(someElementOpt.isPresent());

        XsdElement someElement = someElementOpt.get();
        XsdAnnotation annotation = someElement.getAnnotation();
        List<XsdDocumentation> documentations = annotation.getDocumentations();
        XsdDocumentation xsdDocumentation = documentations.get(0);

        Assert.assertEquals("CDATA line 1\n" +
                "\t\t\tCDATA line 2",xsdDocumentation.getContent());
    }

    /**
     * @return Obtains the filePath of the file associated with this test class.
     */
    private static String getFilePath(){
        URL resource = HtmlParseTest.class.getClassLoader().getResource("issues.xsd");

        if (resource != null){
            return resource.getPath();
        } else {
            throw new RuntimeException("The issues.xsd file is missing from the XsdParser resource folder.");
        }
    }

}
