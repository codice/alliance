Changes between Versions

## 0.3.0
	Release Date: Pending
_This is a preview of a pending release and subject to change._
Built with [DDF Version 2.11.0](https://github.com/codice/ddf/blob/master/CHANGES.md#2110).
    
<h3>Bug</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-193'>CAL-193</a> - As an admin, I want to be able to pause a video transmitter and restart it, and have the stream monitor ingest the video after pausing
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-258'>CAL-258</a> - Large NITF files fail ingest via directory monitor or web upload
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-263'>CAL-263</a> - Isr Mission ID attribute is defined as single-valued but should be multi-valued
	</li>
</ul>

<h3>Story</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-106'>CAL-106</a> - As a user, I want to ingest RFI XML files that follow the MAJIIC 1.71 schema.
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-251'>CAL-251</a> - Add support for PIATGB and PIAPRD TREs
	</li>
</ul>

<h3>Task</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-230'>CAL-230</a> - Add taxonomy attributes for better normalization
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-252'>CAL-252</a> - Upgrade to DDF 2.10.0
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-253'>CAL-253</a> - Remove bower.
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-259'>CAL-259</a> - Export classes from the imaging-transfomer-nitf bundle as OSGI services
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-260'>CAL-260</a> - Upgrade to latest version of Yarn
	</li>
</ul>

## 0.2.1
	Release Date: 2017-03-08
Built with [DDF Version 2.10.1](https://github.com/codice/ddf/blob/master/CHANGES.md#2101)
<h3>Bug</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-258'>CAL-258</a> - Large NITF files fail ingest via directory monitor or web upload
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-263'>CAL-263</a> - Isr Mission ID attribute is defined as single-valued but should be multi-valued
	</li>
</ul>

<h3>Task</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-257'>CAL-257</a> - Clean up maven profiles and add static analysis deactivation
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-260'>CAL-260</a> - Upgrade to latest version of Yarn
	</li>
</ul>

## 0.2.0
	Release Date: 2017-02-08
Built with [DDF Version 2.10.0](https://github.com/codice/ddf/blob/master/CHANGES.md#2100)

<h3>Bug</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-70'>CAL-70</a> - Remove Alliance from app names
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-74'>CAL-74</a> - ResultDAGConverter should handle invalid enum values
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-76'>CAL-76</a> - Replace improper uses of FileBackedOutputStream with TemporaryFileBackedOutputStream
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-80'>CAL-80</a> - Add precision reducer to JTS Geometry
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-83'>CAL-83</a> - fix bad rebase: move tests to new directory
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-84'>CAL-84</a> - klv lat-lon pairs when just one value is error value
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-92'>CAL-92</a> - Change development version to 0.2-SNASHOT and uncomment imaging-nitf-transformer service
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-99'>CAL-99</a> - NSILI outgoing queries incorrectly format geolocation WKT and plain text search on enumerated fields
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-103'>CAL-103</a> - fix build issue from CAL-84
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-108'>CAL-108</a> - Nitf Transformer Fails to Store Overview/Original Image
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-109'>CAL-109</a> - Docs fail to build using Oracle JDK on Linux
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-110'>CAL-110</a> - Unit test TestCatalogOutputAdapter#testGetBinaryContent fails on OpenJDK
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-112'>CAL-112</a> - NSILI endpoint doesn&#39;t correctly handle future start dates
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-114'>CAL-114</a> - Fix incorrectly referenced geowebcache-app in Alliance org.apache.karaf.features.cfg
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-118'>CAL-118</a> - Videographer validator cannot handle scoped ipv6 addresses
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-121'>CAL-121</a> - FrameCenterKlvProcessor doesn&#39;t handle the case of having only one coordinate correctly
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-126'>CAL-126</a> - Cannot store NITF derived images when the ingested NITF title contains invalid special characters
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-128'>CAL-128</a> - Fix commons-lang3 dependency in MgmpTransformer
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-140'>CAL-140</a> - org.codice.ddf.admin.applicationlist.properties file missing from Alliance distribution
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-141'>CAL-141</a> - Jacoco.exec not being generated in bundle target directories
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-142'>CAL-142</a> - Video PacketBuffer TimerTask does not shut down properly
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-148'>CAL-148</a> - imaging-transformer-nitf doesn&#39;t clean up temporary files
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-155'>CAL-155</a> - As an admin, I want the ability to ingest streaming mpeg-ts video generated by ffmpeg
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-157'>CAL-157</a> - Stopping an FMV stream via the Admin Stream Management Plugin does not work
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-159'>CAL-159</a> - Fix synchronization errors with updated NSILI implementation
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-160'>CAL-160</a> - Shutting down an FMV stream is not immediate
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-161'>CAL-161</a> - Unable to ingest NITF images with compression JPEG2000
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-162'>CAL-162</a> - KlvProcessor impls should not add attributes if the type does not match
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-163'>CAL-163</a> - SecurityClassificationKlvProcessor needs to correctly map unsigned byte (short) values to String classifications
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-164'>CAL-164</a> - Set the log level to TRACE level in the sample-mpegts-streamgenerator
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-165'>CAL-165</a> - Alliance root pom points at unused third party repos
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-170'>CAL-170</a> - FederationTest fails with errors initializing sources
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-171'>CAL-171</a> - Video metacards have Media.FRAME_CENTER set but no location set
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-173'>CAL-173</a> - Potential threading issues may exist in the streaming video decoder causing loss or corruption of packets
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-188'>CAL-188</a> - As an admin, I want streaming video to continue ingesting even when a &quot;could not delete&quot; error is thrown
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-191'>CAL-191</a> - Fix sed block in Video Admin Plugin
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-195'>CAL-195</a> - Chip Image action is not available for remote NITF resources
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-196'>CAL-196</a> - Improve video streaming code to be more resilient to UDP packet loss
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-199'>CAL-199</a> - NITF image chips are returned without an extension in a Windows environment
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-200'>CAL-200</a> - Add video/vnd.dlna.mpeg-tts to list of mime-types handled by MpegTsInputTransformer
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-201'>CAL-201</a> - Videographer claims should not be applied to all users
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-208'>CAL-208</a> - Video metacards have a datatype of Document
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-213'>CAL-213</a> - Fix dependencies so artemis-core feature installs correctly
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-214'>CAL-214</a> - Range searches with negative values do not work
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-218'>CAL-218</a> - Update the Pax Exam Version to 4.9.2
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-220'>CAL-220</a> - FMV Video Streams create a metacard from an empty file when they are destroyed
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-228'>CAL-228</a> - change default return address for email notifications to donotreply@example.com
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-234'>CAL-234</a> - Resource URI policy plugin keeps security attributes from being persisted
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-235'>CAL-235</a> - NITF effective time mispopulated with edit time (should be effective time with the nitf.image.imageDateAndTime otherwise known as time over target)
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-236'>CAL-236</a> - The streams that load images for the branding plugin are never closed
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-237'>CAL-237</a> - Image Chip page subtract css margin as an offset during drawing
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-250'>CAL-250</a> - Parse exceptions can occur when ingesting NITFs that have TRE fields with values equal to empty string
	</li>
</ul>

<h3>Story</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-69'>CAL-69</a> - Add Managing documentation for the Alliance  Applications
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-79'>CAL-79</a> - The NITF input transformer should support the parsing of data for AIMIDB TREs
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-87'>CAL-87</a> - Update NITF Input Transformer to use expanded taxonomy
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-90'>CAL-90</a> - update video transformer and udp monitor to use new taxonomy
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-94'>CAL-94</a> - Update the UdpEndpoint to create child metacards with an FMV User Subject
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-119'>CAL-119</a> - Fix Image Chipping
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-132'>CAL-132</a> - Add support for TREs CSEXRA, HISTOA, PIAIMC, CSDIDA to the NITF Input Transformer
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-134'>CAL-134</a> - Store NITF TRE attributes for CSEXRA, PIAIMC, CSDIDA, and HISTOA verbatim
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-137'>CAL-137</a> - As a user, the NSILI OrderMgr interface should support email delivery
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-144'>CAL-144</a> - Duplicate &quot;Export as gmd:MD_Metadata&quot; actions in Action pane on Catalog UI
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-150'>CAL-150</a> - Need to parse and map the NITF FSCLTX header
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-151'>CAL-151</a> - Increment the imaging-nitf library version.
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-172'>CAL-172</a> - Newlines in metadata should be replaced with a space
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-205'>CAL-205</a> - Udp Stream Monitor should support multicast addresses
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-244'>CAL-244</a> - Improve video chunking to better accomodate system load spikes
	</li>
</ul>

<h3>Task</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-57'>CAL-57</a> - Check that DAG is compliant to DataModel before returning
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-64'>CAL-64</a> - Correctly populate NSIL_CARD.status for queries and outgoing DAG
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-65'>CAL-65</a> - As a system administrator, I would like to select the sources to enable on the NSILI endpoint
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-75'>CAL-75</a> - Add logging to video streaming code to provide status during integrations.
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-77'>CAL-77</a> - Implement expanded Taxonomy in Alliance
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-78'>CAL-78</a> - Update NSILI DataModel to STANAG 4559 Ed 3 Amd 2
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-86'>CAL-86</a> - Add Developer documentation for Alliance apps
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-93'>CAL-93</a> - Implement expanded Taxonomy in Alliance
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-95'>CAL-95</a> - Update the Video Stream Admin Plugin to create the stream metacard using its subject
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-96'>CAL-96</a> - Add distance tolerance to Video Admin Plugin
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-97'>CAL-97</a> - Add pom-fix to alliance build
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-98'>CAL-98</a> - Add udp stream generator utility to alliance sdk
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-100'>CAL-100</a> - Remove alliance prefix from alliance-security-app
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-102'>CAL-102</a> - As a user, I want the ability for Alliance to consume MGMPv2 compliant metadata so that I can search and discover MGMPv2 metadata 
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-104'>CAL-104</a> - add configuration for metacard security marking defaults
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-105'>CAL-105</a> - remove unneeded blueprint configurations for video-mpegts-stream
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-111'>CAL-111</a> - Add support for JPEG2000 in the nitf input transformer
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-113'>CAL-113</a> - Update Alliance Pull Request Template to match DDF
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-117'>CAL-117</a> - Refactor NSILI default port to be configurable in system properties
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-120'>CAL-120</a> - Create integration tests for streaming video
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-122'>CAL-122</a> - Use Spock shaded jar and add gmavenplus plugin to root pom
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-124'>CAL-124</a> - Rename test classes in Alliance to *Test.java to follow testing practices guidelines
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-129'>CAL-129</a> - Add javadoc and sources to the release
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-130'>CAL-130</a> - Create integration tests for Alliance imaging
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-131'>CAL-131</a> - Add support for all NITF fields to map to a metacard attribute
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-133'>CAL-133</a> - Refactor reusable test code from itest modules
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-135'>CAL-135</a> - The sample-nsili-server and sample-nsili-client packages should use slf4j over system.out
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-143'>CAL-143</a> - Incorporate Catalog UI CSS Styling into Image Chipping Application
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-147'>CAL-147</a> - Remove alliance-app and mgmp transformers
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-152'>CAL-152</a> - Enable documentation to be skipped during development
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-153'>CAL-153</a> - Update DefaultSecurityAttributeValuesPlugin to use classification instead of clearance
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-154'>CAL-154</a> - Automate Sample NSILI Client Test Cases into Integration Tests to Expand Test Coverage
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-156'>CAL-156</a> - Institute a CHANGELOG for Alliance releases
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-189'>CAL-189</a> - Add ImageMetacardType and GtmiMetacardType JUnit tests
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-190'>CAL-190</a> - Update NitfAttributes to accept a fully qualified attribute name instead of adding prefixes
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-194'>CAL-194</a> - Upgrade frontend-maven-plugin from 0.0.28 to 1.2
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-197'>CAL-197</a> - Remove Jacoco line coverage thresholds
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-198'>CAL-198</a> - Uploaded products without security markings should be rejected and should update the ingest log
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-203'>CAL-203</a> - Configure OWASP to run on Alliance
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-206'>CAL-206</a> - Create a summary view of supported document formats for ingest
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-207'>CAL-207</a> - Update MPEG-TS Content Resolver to handle .mpg and .mpeg extensions
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-209'>CAL-209</a> - Update NSILI  to new Versioning API
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-211'>CAL-211</a> - Update NitfParserAdapter to get headers only when returning the NitfSegmentsFlow
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-212'>CAL-212</a> - Align dependency version in Documentation maven profile
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-215'>CAL-215</a> - Changes to security attributes on a metacard should be audited
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-216'>CAL-216</a> - Update codice-nitf-imaging library version to 0.6
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-227'>CAL-227</a> - Refactor FMV code to close the Netty ChannelFuture when shutting down the stream
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-229'>CAL-229</a> - Fix the Alliance OWASP build
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-231'>CAL-231</a> - Fix mvn coordinate for search-ui-app
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-232'>CAL-232</a> - Document Using Landing Page, Simple Search UI, and Catalog UI
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-233'>CAL-233</a> - Add content to core concepts section in documentation
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-239'>CAL-239</a> - Update Configuring Security and Hardening documentation
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-240'>CAL-240</a> - Prepare release documentation
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-242'>CAL-242</a> - replace documentation.adoc contents with draft-documentation.adoc
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-247'>CAL-247</a> - Replace npm with yarn
	</li>
</ul>


## 0.1.2
	Release Date: 2017-02-17
<h3>Bug</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-155'>CAL-155</a> - As an admin, I want the ability to ingest streaming mpeg-ts video generated by ffmpeg
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-191'>CAL-191</a> - Fix sed block in Video Admin Plugin
	</li>
</ul>

<h3>Task</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-131'>CAL-131</a> - Add support for all NITF fields to map to a metacard attribute
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-202'>CAL-202</a> - Create a summary view of supported document formats for ingest
	</li>
</ul>



## 0.1.1
	Release Date: 2016-10-26
<h3>Bug</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-74'>CAL-74</a> - ResultDAGConverter should handle invalid enum values
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-76'>CAL-76</a> - Replace improper uses of FileBackedOutputStream with TemporaryFileBackedOutputStream
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-80'>CAL-80</a> - Add precision reducer to JTS Geometry
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-83'>CAL-83</a> - fix bad rebase: move tests to new directory
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-84'>CAL-84</a> - klv lat-lon pairs when just one value is error value
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-99'>CAL-99</a> - NSILI outgoing queries incorrectly format geolocation WKT and plain text search on enumerated fields
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-103'>CAL-103</a> - fix build issue from CAL-84
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-109'>CAL-109</a> - Docs fail to build using Oracle JDK on Linux
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-114'>CAL-114</a> - Fix incorrectly referenced geowebcache-app in Alliance org.apache.karaf.features.cfg
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-121'>CAL-121</a> - FrameCenterKlvProcessor doesn&#39;t handle the case of having only one coordinate correctly
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-162'>CAL-162</a> - KlvProcessor impls should not add attributes if the type does not match
	</li>
</ul>

<h3>Task</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-57'>CAL-57</a> - Check that DAG is compliant to DataModel before returning
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-64'>CAL-64</a> - Correctly populate NSIL_CARD.status for queries and outgoing DAG
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-75'>CAL-75</a> - Add logging to video streaming code to provide status during integrations.
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-78'>CAL-78</a> - Update NSILI DataModel to STANAG 4559 Ed 3 Amd 2
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-96'>CAL-96</a> - Add distance tolerance to Video Admin Plugin
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-97'>CAL-97</a> - Add pom-fix to alliance build
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-100'>CAL-100</a> - Remove alliance prefix from alliance-security-app
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-105'>CAL-105</a> - remove unneeded blueprint configurations for video-mpegts-stream
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-117'>CAL-117</a> - Refactor NSILI default port to be configurable in system properties
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-122'>CAL-122</a> - Use Spock shaded jar and add gmavenplus plugin to root pom
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-149'>CAL-149</a> - Upgrade to DDF 2.9.3
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-169'>CAL-169</a> - Upgrade Allance 0.1.1 to DDF 2.9.3
	</li>
</ul>

## 0.1.0
	Release Date: 2016-06-30
<h3>Bug</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-25'>CAL-25</a> - Alliance build failing due to incorrect catalog pom artifact id
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-27'>CAL-27</a> - As an administrator I would like cleaner log information about &quot;unavailable&quot; federations so that I can more easily diagnose failures and coordinate solutions in federations
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-31'>CAL-31</a> - DDF SolrFilterDelegate needs additional query methods implemented to support STANAG 4559
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-34'>CAL-34</a> - Nitf Pre-Storage Plugin Test Failing
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-35'>CAL-35</a> - JaCoCo output directory is not named correctly
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-39'>CAL-39</a> - As a user, I want to see thumbnails for STANAG-4559 results
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-40'>CAL-40</a> - Add the ability to programmatically swap x/y coordinate values for STANAG 4559 sources
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-68'>CAL-68</a> - NitfPreStoragePlugin should create a scaled overview image
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-71'>CAL-71</a> - The image module in Alliance was refactored, and the metatype for image-transformer-nitf was not included in the refactoring
	</li>
</ul>

<h3>Story</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-11'>CAL-11</a> - As an integrator, I want an endpoint that will accept STANAG 4559 compliant queries and return compliant result sets.
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-13'>CAL-13</a> - As an integrator, I want a STANAG 4559 Endpoint
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-14'>CAL-14</a> - As an integrator, I want a STANAG 4559 endpoint that supports the CatalogMgr interface
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-15'>CAL-15</a> - As an integrator, I want a STANAG 4559 endpoint that supports the OrderMgr interface
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-16'>CAL-16</a> - As an integrator, I want a STANAG 4559 endpoint that supports the ProductMgr interface
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-17'>CAL-17</a> - As an integrator, I want a STANAG 4559 endpoint that supports the DataModelMgr interface
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-18'>CAL-18</a> - As an integrator, I want a STANAG 4559 endpoint that supports the StandingQueryMgr interface
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-19'>CAL-19</a> - As an integrator, I want a STANAG 4559 endpoint that converts metacards to NSILI DAG
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-22'>CAL-22</a> - As an integrator, I want the NSILI source to have the functionality to use a local IOR txt file
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-30'>CAL-30</a> - Update the default mime type mappings for NISF data
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-36'>CAL-36</a> - Ingest MPEG-TS UDP Stream
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-42'>CAL-42</a> - As an administrator, I want to view the current status of my imagery feeds in Alliance
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-46'>CAL-46</a> - remove subsample count field from the alliance-mpegts-stream config page
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-47'>CAL-47</a> - make parent metacard as soon as configured in alliance-mpegts-stream
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-48'>CAL-48</a> - Refactor the Nitf Input Transformer to Use imaging-nitf 0.3
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-49'>CAL-49</a> - The NITF input transformer should support the parsing and ingesting of data for ACFTB and MITRPB TREs. 
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-50'>CAL-50</a> - update libs/klv to use new isError methods
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-52'>CAL-52</a> - unflushed data in mpeg-ts udp stream monitor
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-54'>CAL-54</a> - reduce the location and frame center data
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-55'>CAL-55</a> - Create automated integration tests for the Alliance NSILI Federated Source
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-59'>CAL-59</a> - Alliance NSILI endpoint should be able to control the ports it listens to for CORBA requests
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-73'>CAL-73</a> - As a user, I want to extract banner markings from text documents and apply them to metacards
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-137'>CAL-137</a> - As a user, the NSILI OrderMgr interface should support email delivery
	</li>
</ul>

<h3>Task</h3>
<ul>
	<li><a href='https://codice.atlassian.net/browse/CAL-20'>CAL-20</a> - Provide the ability to extract KLV from MPEG-2 transport streams that adhere to the STANAG 4609 standard
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-23'>CAL-23</a> - Fix NSILI Source query problems
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-26'>CAL-26</a> - Correct maven release plugin version in root pom
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-41'>CAL-41</a> - Provide the option to &quot;drape&quot; a NITF overview on the map
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-44'>CAL-44</a> - transfer to Codice repo and rebrand
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-51'>CAL-51</a> - As an system administrator, I would like additional debug information for STANAG-4559 sources
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-53'>CAL-53</a> - Implement a metacard action to chip NITF imagery
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-56'>CAL-56</a> - NSILI StandingQuery should only return requested attributes
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-60'>CAL-60</a> - As an integrator, I want the ability to configure a NSILI source using an FTP address
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-61'>CAL-61</a> - Add the ability to control federated sources that are queried with the NSILI endpoint
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-62'>CAL-62</a> - ORB shutdown doesn&#39;t work correctly on NSILI Endpoint
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-63'>CAL-63</a> - Create an ORB service that both the NSILI Endpoint and NSILI Source can share
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-81'>CAL-81</a> - Clean up obsolete dependencies in Alliance
	</li>
	<li><a href='https://codice.atlassian.net/browse/CAL-156'>CAL-156</a> - Institute a CHANGELOG for Alliance releases
	</li>
</ul>
