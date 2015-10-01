package org.percepta.mgrankvi.spelling.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
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

    @Override
    protected void init(VaadinRequest request) {

        // Initialize our new UI component
        TextField component = new TextField("Spell checked");
        component.setImmediate(true);

        new SpellCheck().extend(component);

        final TextField swe = new TextField("Swedish");
        new SpellCheck("sv.dic", true).extend(swe);

        TextArea area = new TextArea("An area");
        new SpellCheck().extend(area);

        // Show it in the middle of the screen
        final VerticalLayout layout = new VerticalLayout();
        layout.setStyleName("demoContentLayout");
        layout.setSizeFull();
        layout.addComponents(component, swe, area);
        layout.setComponentAlignment(swe, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(component, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(area, Alignment.MIDDLE_CENTER);
        setContent(layout);

    }

}
