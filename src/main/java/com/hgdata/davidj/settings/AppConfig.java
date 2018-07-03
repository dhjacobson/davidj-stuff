package com.hgdata.davidj.settings;

import org.aeonbits.owner.Config;

@Config.Sources({"file:AppConfig.properties"})
public interface AppConfig extends Config {

    //AWIS
    @Key("aws.awis.profile")
    @DefaultValue("default")
    String awisProfileName();

    //MRD on Athena
    @Key("aws.athena.region")
    @DefaultValue("us-west-2")
    String athenaRegion();

    @Key("aws.athena.mrd_schema")
    @DefaultValue("mrdexport20170928185915_d8f4")
    String athenaMrdSchema();

    @Key("aws.athena.data_release_schema")
    @DefaultValue("data_release_2017_11_01")
    String athenaDataReleaseSchema();


}
