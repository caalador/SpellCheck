package org.percepta.mgrankvi.spelling.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.percepta.mgrankvi.spelling.SpellCheck;

import javax.servlet.annotation.WebServlet;

@Theme("demo")
@Title("MyComponent Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.percepta.mgrankvi.spelling.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

//    SpellCheck textCheck;
//    SpellCheck areaCheck;
VerticalLayout fields;
    final VerticalLayout layout = new VerticalLayout();

    @Override
    protected void init(VaadinRequest request) {
        final NativeSelect languages = new NativeSelect("Language selection");
        languages.addItem("en");
        languages.addItem("sv");
        languages.addItem("fi");

        languages.setNullSelectionAllowed(false);
        languages.select("en");

        languages.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                layout.removeComponent(fields);
                fields = createFields((String) languages.getValue());
                layout.addComponent(fields);
                layout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);
            }
        });


        // Show it in the middle of the screen

        layout.setStyleName("demoContentLayout");
        layout.setSizeFull();
        layout.addComponents(languages);

        fields = createFields((String) languages.getValue());
        layout.addComponent(fields);

        layout.setComponentAlignment(languages, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);

        setContent(layout);
    }

    public VerticalLayout createFields(String language) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        final TextField component = new TextField("Spell checked");
        component.setImmediate(true);
        final TextArea area = new TextArea("A spell checked text area");
        // Initialize our new UI component
        component.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                System.out.println("Value changed to: " + event.getProperty().getValue().toString());
            }
        });

        new SpellCheck(language).extend(component);
        new SpellCheck(language).extend(area);

        layout.addComponents(component, area);
        layout.setComponentAlignment(component, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(area, Alignment.MIDDLE_CENTER);

        return layout;
    }

}
