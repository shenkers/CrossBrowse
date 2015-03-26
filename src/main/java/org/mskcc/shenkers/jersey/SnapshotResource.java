/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.jersey;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mskcc.shenkers.controller.FXMLController;
import org.mskcc.shenkers.model.ModelSingleton;
import org.mskcc.shenkers.model.datatypes.Genome;
import org.mskcc.shenkers.model.datatypes.GenomeSpan;

/**
 *
 * @author sol
 */
@Path("getSnapshot")
public class SnapshotResource {

    Logger log = LogManager.getLogger();

    FXMLController view;

    @GET
    @Produces("image/png")
    public Response getSnapshot() {
        try {

            CountDownLatch l = new CountDownLatch(1);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BooleanProperty isBusy = new SimpleBooleanProperty(true);

            log.info("creating listener");
            // listen until the status is no longer busy, then write the 
            // snapshot to an image
            ChangeListener<Boolean> renderStatusListener = new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    Platform.runLater(() -> {

                        WritableImage snapshot = view.getSnapshotNode().snapshot(new SnapshotParameters(), null);
                        BufferedImage fromFXImage = SwingFXUtils.fromFXImage(snapshot, null);
                        try {
                            ImageIO.write(fromFXImage, "png", baos);
                        } catch (IOException ex) {
                            log.error("exception", ex);
                        }
                        isBusy.removeListener(this);
                        l.countDown();
                    });
                }
            };

            log.info("adding listener");
            isBusy.addListener(renderStatusListener);
            
            log.info("view is busy {}", view.getIsBusy());
            // bind the status to the view
            isBusy.bind(view.getIsBusy());

            view.getIsBusy().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    log.info("changed!");
                }
            });
            l.await();

            byte[] imageData = baos.toByteArray();
            // uncomment line below to send non-streamed
            return Response.ok(imageData).build();

            // uncomment line below to send streamed
            // return Response.ok(new ByteArrayInputStream(imageData)).build();
        } catch (InterruptedException ex) {
            log.info("error", ex);
        }
        return Response.serverError().build();
    }

//    @GET
//    @Produces("image/png")
    public Response getSnapshotNow() {
        try {

            CountDownLatch l = new CountDownLatch(1);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Platform.runLater(() -> {
                WritableImage snapshot = view.getSnapshotNode().snapshot(new SnapshotParameters(), null);
                BufferedImage fromFXImage = SwingFXUtils.fromFXImage(snapshot, null);
                try {
                    ImageIO.write(fromFXImage, "png", baos);
                } catch (IOException ex) {
                    log.error("exception", ex);
                }
                l.countDown();
            });

            l.await();

            byte[] imageData = baos.toByteArray();
            // uncomment line below to send non-streamed
            return Response.ok(imageData).build();

            // uncomment line below to send streamed
            // return Response.ok(new ByteArrayInputStream(imageData)).build();
        } catch (InterruptedException ex) {
            log.info("error", ex);
        }
        return Response.serverError().build();
    }

//    @GET
////    @Produces (MediaType.APPLICATION_JSON)
//    public Response setCoordinate(
//            @QueryParam("genomeId") String genomeId,
//            @QueryParam("neg") boolean toNegativeStrand
//    ) {
//        log.info("id={} neg={}",genomeId,toNegativeStrand);
//        log.info("recieved get");
//        return Response.ok().build();
//    }
}
