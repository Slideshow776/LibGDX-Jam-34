package no.sandramoen.libgdx34.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Panel;

import no.sandramoen.libgdx34.MyGdxGame;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
        @Override
        public GwtApplicationConfiguration getConfig () {
            // Resizable application, uses available space in browser with no padding:
            /*GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(true);
            cfg.padVertical = 0;
            cfg.padHorizontal = 0;
            return cfg;*/
            // If you want a fixed size application, comment out the above resizable section,
            // and uncomment below:
               GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(930, 930);
               cfg.padHorizontal = 0;
               cfg.padVertical = 0;
          return cfg;
        }

        @Override
        public ApplicationListener createApplicationListener () {
            return new MyGdxGame();
        }


        @Override
        public Preloader.PreloaderCallback getPreloaderCallback() {
            return createPreloaderPanel(GWT.getHostPageBaseURL() + "preloadlogo.png");
        }


        @Override
        protected void adjustMeterPanel(Panel meterPanel, Style meterStyle) {
            meterPanel.setStyleName("gdx-meter");
            meterPanel.addStyleName("nostripes");
            meterStyle.setProperty("backgroundColor", "#ffffff");
            meterStyle.setProperty("backgroundImage", "none");
        }
}
