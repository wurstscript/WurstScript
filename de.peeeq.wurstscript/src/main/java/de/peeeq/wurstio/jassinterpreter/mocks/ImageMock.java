package de.peeeq.wurstio.jassinterpreter.mocks;

import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import de.peeeq.wurstscript.intermediatelang.ILconstString;

public class ImageMock {
    private final ILconstString file;
    private final ILconstReal sizeX;
    private final ILconstReal sizeY;
    private final ILconstReal sizeZ;
    private ILconstReal posX;
    private ILconstReal posY;
    private ILconstReal posZ;
    private final ILconstReal originX;
    private final ILconstReal originY;
    private final ILconstReal originZ;
    private final ILconstInt imageType;

    private ILconstBool shown = ILconstBool.FALSE;

    public ImageMock(ILconstString file, ILconstReal sizeX, ILconstReal sizeY, ILconstReal sizeZ, ILconstReal posX, ILconstReal posY, ILconstReal posZ,
                     ILconstReal originX, ILconstReal originY, ILconstReal originZ, ILconstInt imageType) {

        this.file = file;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.imageType = imageType;
    }

    public ILconstString getFile() {
        return file;
    }

    public ILconstReal getSizeX() {
        return sizeX;
    }

    public ILconstReal getSizeY() {
        return sizeY;
    }

    public ILconstReal getSizeZ() {
        return sizeZ;
    }

    public ILconstReal getPosX() {
        return posX;
    }

    public ILconstReal getPosY() {
        return posY;
    }

    public ILconstReal getPosZ() {
        return posZ;
    }

    public ILconstReal getOriginX() {
        return originX;
    }

    public ILconstReal getOriginY() {
        return originY;
    }

    public ILconstReal getOriginZ() {
        return originZ;
    }

    public ILconstInt getImageType() {
        return imageType;
    }

    public ILconstBool isShown() {
        return shown;
    }

    public void setShown(ILconstBool shown) {
        this.shown = shown;
    }

    public void setPosition(ILconstReal x, ILconstReal y, ILconstReal z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }
}
