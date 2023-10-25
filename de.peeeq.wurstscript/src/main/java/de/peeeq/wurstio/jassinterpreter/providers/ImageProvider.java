package de.peeeq.wurstio.jassinterpreter.providers;

import de.peeeq.wurstio.jassinterpreter.mocks.ImageMock;
import de.peeeq.wurstscript.intermediatelang.*;
import de.peeeq.wurstscript.intermediatelang.interpreter.AbstractInterpreter;

public class ImageProvider extends Provider {

    public ImageProvider(AbstractInterpreter interpreter) {
        super(interpreter);
    }

    public IlConstHandle CreateImage(ILconstString file, ILconstReal sizeX, ILconstReal sizeY, ILconstReal sizeZ, ILconstReal posX, ILconstReal posY,
                                     ILconstReal posZ, ILconstReal originX, ILconstReal originY, ILconstReal originZ, ILconstInt imageType) {
        return new IlConstHandle(NameProvider.getRandomName("image"), new ImageMock(file, sizeX, sizeY, sizeZ, posX, posY, posZ,
                                                                                            originX, originY, originZ, imageType));
    }

    public void DestroyImage(IlConstHandle image) {
    }

    public void ShowImage(IlConstHandle image, ILconstBool flag) {
        ImageMock imageMock = (ImageMock) image.getObj();
        imageMock.setShown(flag);
    }

    public void SetImagePosition(IlConstHandle image, ILconstReal x, ILconstReal y, ILconstReal z) {
        ImageMock imageMock = (ImageMock) image.getObj();
        imageMock.setPosition(x, y, z);
    }

    public void SetImageRender(IlConstHandle image, ILconstBool flag) {
    }

    public void SetImageRenderAlways(IlConstHandle image, ILconstBool flag) {
    }

    public void SetImageAboveWater(IlConstHandle image, ILconstBool flag, ILconstBool useWaterAlpha) {
    }

    public void SetImageType(IlConstHandle image, ILconstInt type) {
    }

}
