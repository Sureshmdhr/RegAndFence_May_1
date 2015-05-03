package com.suresh.routing;

import org.mapsforge.core.model.LatLong;

public class POI
{
  private double lat;
  private double lng;
  private String name;
  private String type;
  private LatLong latlong;
  private String id;

  public POI(double lats, double lgts, String paramString)
  {
    this.lat = lats;
    this.lng = lgts;
    this.name = paramString;
  }

  public POI(double lats, double lgts, String name, String type)
  {
    this.lat = lats;
    this.lng = lgts;
    this.name = name;
    this.type = type;
  }
  
  public POI(double lats, double lgts, String name, String type,String id)
  {
    this.lat = lats;
    this.lng = lgts;
    this.name = name;
    this.type = type;
    this.id=id;
  }

  public double getLat()
  {
    return this.lat;
  }

  public double getLng()
  {
    return this.lng;
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public LatLong getLatLng()
  {
	  this.latlong=new LatLong(this.lat,this.lng);
    return latlong;
  }

  public String getName()
  {
    return this.name;
  }

  public String getType()
  {
    return this.type;
  }

  public void setLat(double paramDouble)
  {
    this.lat = paramDouble;
  }

  public void setLng(double paramDouble)
  {
    this.lng = paramDouble;
  }

  public void setName(String paramString)
  {
    this.name = paramString;
  }

  public void setType(String paramString)
  {
    this.type = paramString;
  }
  
  public void setId(String paramString)
  {
    this.id = paramString;
  }
}