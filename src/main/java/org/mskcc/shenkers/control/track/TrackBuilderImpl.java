/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.shenkers.control.track;

import org.mskcc.shenkers.control.track.bam.BamContext;
import com.google.inject.Inject;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broad.igv.bbfile.BBFileReader;
import org.mskcc.shenkers.control.track.bigwig.BigWigContext;
import org.mskcc.shenkers.control.track.fasta.FastaContext;
import org.mskcc.shenkers.control.track.gene.GTFContext;
import org.mskcc.shenkers.control.track.gene.GTFGeneModelProvider;
import org.mskcc.shenkers.control.track.gene.GeneModelContext;
import org.mskcc.shenkers.control.track.gene.GeneModelProvider;
import org.mskcc.shenkers.control.track.interval.BEDIntervalProvider;
import org.mskcc.shenkers.control.track.interval.IntervalContext;
import org.mskcc.shenkers.control.track.interval.IntervalProvider;

/**
 *
 * @author sol
 */
public class TrackBuilderImpl implements TrackBuilder {

    Logger logger = LogManager.getLogger();

    TrackFactory<BamContext> btf;
    TrackFactory<BigWigContext> wtf;
    TrackFactory<GeneModelContext> gtf;
    TrackFactory<IntervalContext> itf;
    TrackFactory<FastaContext> ftf;

    @Inject
    public TrackBuilderImpl(TrackFactory<BamContext> btf, TrackFactory<BigWigContext> wtf, TrackFactory<GeneModelContext> gtf, TrackFactory<IntervalContext> itf, TrackFactory<FastaContext> ftf) {
        this.btf = btf;
        this.wtf = wtf;
        this.gtf = gtf;
        this.itf = itf;
        this.ftf = ftf;
    }

    public Track load(FileType type, String resource) {
        switch (type) {
            case BAM: {
                System.out.println("loading " + type + " " + resource);
                SamReader reader = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(new File(resource));
                BamContext context = new BamContext(reader);
                return btf.create(context);
            }
            case WIG: {
                System.out.println("loading " + type + " " + resource);
                BBFileReader reader = null;
                try {
                    reader = new BBFileReader(resource);
                } catch (IOException ex) {
                    logger.error("Error loading bigwig file", ex);
                }
                BigWigContext context = new BigWigContext(reader);
                return wtf.create(context);
            }
            case GTF: {
                System.out.println("loading " + type + " " + resource);
                GeneModelProvider modelProvider = null;
                try {
                    modelProvider = new GTFGeneModelProvider(resource, false, false);
                } catch (IOException e) {
                    logger.error("Error loading " + type + " file", e);
                }
                GeneModelContext context = new GeneModelContext(modelProvider);

                return gtf.create(context);
            }
            case BED: {
                System.out.println("loading " + type + " " + resource);
                IntervalProvider modelProvider = null;
                try {
                    modelProvider = new BEDIntervalProvider(resource, false, false);
                } catch (IOException e) {
                    logger.error("Error loading " + type + " file", e);
                }
                IntervalContext context = new IntervalContext(modelProvider);

                return itf.create(context);
            }
            case FASTA: {
                logger.info("loading " + type + " " + resource);

                FastaContext context = null;
                try {
                    context = new FastaContext(resource, false);
                } catch (FileNotFoundException ex) {
                    logger.error("error loading fasta", ex);
                } catch (IOException ex) {
                    logger.error("error loading fasta", ex);
                }

                return ftf.create(context);
            }
            default: {
                throw new RuntimeException("No handler configured for " + type);
            }
        }
    }
}
