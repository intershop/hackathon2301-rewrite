package com.intershop.application.responsive.pipelet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.intershop.application.responsive.internal.image.Dimension;
import com.intershop.beehive.core.capi.file.FileUtils;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.request.Request;

/**
 * The pipelet determines the width and height of an image.
 * 
 */
public final class DetermineImageDimension extends Pipelet
{
    private static final String IMAGE_DIMENSION = "ImageDimension";
    
    private static final String CONTENT_REF = "ContentRef";

    @Override
    public int execute(final PipelineDictionary dict) throws PipeletExecutionException
    {
        final String contentRef = dict.getRequired(CONTENT_REF);
        
        final Optional<File> imageFile = determineImageFile(contentRef);
        
        final Optional<Dimension> imageDimension = determineImageDimension(imageFile);
        
        if(imageDimension.isPresent())
        {
            dict.put(IMAGE_DIMENSION, imageDimension.get());
        }
        
        return PIPELET_NEXT;
    }

    private Optional<File> determineImageFile(final String contentRef)
    {
        
        if(StringUtils.isNotEmpty(contentRef))
        {
            final List<String> splittedContentRef = Arrays.asList(contentRef.split(":"));
            
            final String domainName = Iterables.getFirst(splittedContentRef, null);
            final String imagePath = Iterables.getLast(splittedContentRef, null);
            
            if(StringUtils.isNotEmpty(domainName)
                            && StringUtils.isNotEmpty(imagePath))
            {
                final String unitStaticContentDirectory = FileUtils.getUnitStaticContentDirectory(domainName);
                
                final Request currentReq = Request.getCurrent();
                final String localeID = currentReq.getLocale().getLocaleID();

                return Optional.ofNullable(new File(new File(unitStaticContentDirectory, localeID), imagePath));
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<Dimension> determineImageDimension(final Optional<File> imageFile)
    {
        if (imageFile.isPresent())
        {
            final File file = imageFile.get();
            if (file.exists()) 
            {
                try (ImageInputStream in = ImageIO.createImageInputStream(file))
                {
                    if (in != null) 
                    {
                        final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
                        if (readers.hasNext())
                        {
                            ImageReader reader = readers.next();
                            try
                            {
                                reader.setInput(in);
                                final int height = reader.getHeight(0);
                                final int width = reader.getWidth(0);
                                return Optional.ofNullable(new Dimension(width, height));
                            }
                            finally
                            {
                                reader.dispose();
                            }
                        }
                    }
                }
                catch(final IOException e)
                {
                    return Optional.empty();
                }
            }
        }
        
        return Optional.empty();
    }
}
