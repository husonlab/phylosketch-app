module phylosketch {
	requires transitive com.gluonhq.attach.display;
	requires transitive com.gluonhq.attach.storage;
	requires transitive com.gluonhq.attach.util;
	requires transitive com.gluonhq.charm.glisten;
	requires transitive com.gluonhq.attach.share;
	requires transitive com.gluonhq.attach.lifecycle;
	requires transitive javafx.graphics;
	requires transitive jloda2;

	exports org.husonlab.phylosketch;

	opens org.husonlab.phylosketch;
	opens org.husonlab.phylosketch.network;
	opens org.husonlab.phylosketch.utils;
	opens org.husonlab.phylosketch.views.log;
	opens org.husonlab.phylosketch.views.menu;
	opens org.husonlab.phylosketch.views.primary;
	opens org.husonlab.phylosketch.views.labeleditor;
	opens org.husonlab.phylosketch.views.secondary;
}