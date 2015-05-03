package com.suresh.extras;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

import android.content.Context;

public class TappableMarker extends Marker
{
	public TappableMarker(Context  context,int user, LatLong localLatLong)
	{ 
		super(localLatLong,
				AndroidGraphicFactory.convertToBitmap(context.getResources().getDrawable(user)),
				0,
				-1*(AndroidGraphicFactory.convertToBitmap(context.getResources().getDrawable(user)).getHeight())/2);
	}
	
	public TappableMarker(Context  context,int user, LatLong localLatLong,String pos)
	{ 
		super(localLatLong,
				AndroidGraphicFactory.convertToBitmap(context.getResources().getDrawable(user)),
				0,0);
	}
	
	public boolean onTap(LatLong tapLatLong,
			org.mapsforge.core.model.Point layerXY,
			org.mapsforge.core.model.Point tapXY) 
	{
		return super.onTap(tapLatLong, layerXY, tapXY);
	}
}