package com.intershop.application.responsive.internal.image;

public final class Dimension
{
    private final int width;
    
    private final int height;
    
    public Dimension(final int width, final int height)
    {
        this.width = width;
        this.height = height;
    }
    
    public int getWidth()
    {
        return this.width;
    }
    
    public int getHeight()
    {
        return this.height;
    }
}