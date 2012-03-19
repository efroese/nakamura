rSmart Nakamura Changelog
=========================

base-1.1.3-rsmart
-----------------
* KERN-2683 corrected merge error df4deff
* KERN-2683 style compliance 69b1943
* KERN-2683 moved a bracket :) 94ecfc1
* KERN-2683 addressed PR feedback: threadsafe CopyOnWriteArrayList used, licenses added, code style confomance, and added Exception construc
* KERN-2683 removed diff artifact left over from merge dbc5f3e
* KERN-2683 merged authorizable index worker framework Conflicts: 85734aa
* ACAD-855 renamed binding for indexing workers to authorizableIndexingWorker f2f1ce3
* ACAD-855 exported new .indexing api package 5d8ee6e
* ACAD-855 wired AuthorizableIndexingWorkers into AuthorizableIndexingHandler e784e58
* ACAD-855 created interfaces for plugin framework bcb8a71
* ACAD-790 repaired logic to grab external course id from world 79c724e
* ACAD-826 Enable sakai.blogwow, sakai.sitestats, and sakai.sections by default in Sakai 2 Tools Widget d3a7b99
* NOJIRA post release pooch clean up 6d6a83d
* NOJIRA fix release post clean up 908209a
* release_post_process: Moving config files from 1.1.2-rsmart to 1.1.3-rsmart-SNAPSHOT-SNAPSHOT 259dce3
* NOJIRA revert release issues da2108c
* Revert "NOJIRA working through more release issues" 4855775
* Revert "NOJIRA working through release issues" 54cb2da
* [maven-release-plugin] prepare for next development iteration 29da64f

base-1.1.2-rsmart
-----------------
* [maven-release-plugin] prepare release base-1.1.2-rsmart c9fceef
* NOJIRA more release issues try a strong arm d0d2d97
* NOJIRA working through more release issues 7375d2e
* Revert "NOJIRA working through release issues" 7c32148
* [maven-release-plugin] prepare release org.sakaiproject.nakamura.mysqljdbc-1.1.2-rsmart 753fc90
* NOJIRA working through release issues 0838045
* NOJIRA prepare for release 3d7248e
* release_pre_process: Moving config files from 1.1.2-rsmart-SNAPSHOT to 1.1.2-rsmart 49d7e40
* NOJIRA small syntax fix b078c2e
* KERN-1989 Add activate/deactivate methods. (cherry picked from commit 2a0f8075bdbd565c5e91394723b0d9e785f2a54d) 4499304
* KERN-1989 Add property to allow gzip filter to be disabled using configuration since this isn't an OSGi feature. Add annotations that can be used to generate the osgi files. Add metatype files for console configuring. (cherry picked from commit 60969f434b80c8354341878c8843319f2ec218a3) e850f24
* ACAD-791 altered logic for generating LTI context ID. Can now be set on a group via sakai:cle-site, or on an 'area' or document with lti_context_id 8f2192a
* ACAD-790 fix build... added group property "sakai:external-course-id" to payload as "custom_external_course_id" for sis integration ce63a3b
* ACAD-790 added group property "sakai:external-course-id" to payload as "custom_external_course_id" for sis integration e5aca8a
* NOJIRA bind user bundle to sparse@1.3.3-rsmart-SNAPSHOT 1714536
* NOJIRA switch sparse and solr to SNAPSHOT versions d558a4b
* NOJIRA rev release plugin to 2.2.2 0b52b1a
* Revert "ACAD-783 skip gpg signing" 29a32e2
* ACAD-65 Remove the SparseUserFinderImpl and remove the reference constraint. d52bcc1
* ACAD-783 skip gpg signing 8c86ade
* Revert "ACAD-783 remove gpg signing of artifacts" 7986c34
* ACAD-783 remove gpg signing of artifacts ec9e5fa
* NOJIRA bundle install --without preview_processor 6eaa587
* NOJIRA add rsmart-dev nexus repo ff255be
* ACAD-783 Add support for Maven Release plugin 4d53d40
* KERN-2599 Improve log output from release_*_process.sh scripts (cherry picked from commit e552760386bfd3a83a15797063ea415c18755314) 1ed6378
* Merge pull request #563 from thecarlhall/fix-gemfile-lock 3a1af8c
* ACAD-781 Rejigger maven pom versions of build artifacts to reflect that they are SNAPSHOT builds cc917fd
* switching from version to next SNAPSHOT in config files d62fb4d
* KERN-2598 When creating a members-only world, the main group should be a viewer of subgroups (cherry picked from commit 29abd7ccca7ec98b57e88c07204c82fddf710b5e) c2639ba
* ACAD-65 Bind to core 1.3.2-rsmart 3aed07a
* ACAD-65 Move OverridenRequest to its own file. Test it Test the CaseInsensitiveAuthFilter. bb9e132
* ACAD-65 Add test. Fix a bug in ci lookup. a79490f
* ACAD-96 created a debug checkbox in the CAPTCHA config. When checked CAPTCHA verification is ignored. 7bda45e
* NOJIRA rev version number for 1.1.2 sprint 06f6b92
* ACAD-772 merge automated integration test 95107a0
* [#ACAD-772] test commit 48825e6
* ACAD-772 test 123 ffa76ec
* KERN-2316 properly require rubygems and bundler everywhere (cherry picked from commit 257af45384994cf2ae0773f509f9e01ea6bc3a08) d0063dc
* KERN-2316 properly require rubygems and bundler everywhere (cherry picked from commit 61dee212eb4cc300ac3860583566931b3d1dbb86) 11a3574
* KERN-2520 remove curb from dependencies and doc (cherry picked from commit 201a240617898eec38a2dc6fe0842aabc0b3ffbe) f311854
* NOJIRA make the ruby tests quiet, so we don't get output we don't want while running all tests. (cherry picked from commit d5d5ddc02d6b0127df26a37dccd17c7c66f55646) c024b77
* KERN-2487 Standardize uniqueness mechanism. Eliminate use of Time.now.to_i. Fix some scripting bugs. (cherry picked from commit 1861ae2406a7c64e018663e69ac28056aa6da8c5) a0031d9
* add some ant tasks for checking port 8080 and ensuring nakamura shuts down (cherry picked from commit 63778c3913f186ee4fa9cc91adb8beab1175e620) 376c008
* fix a couple of typos in the README file (cherry picked from commit be4f76b9876fc59d934ed3cabe5ba38d14aa8a61) 84faed1
* add a README file for the integration module (cherry picked from commit 0722d16394d83b9bd74a106099547a49e60be4b4) 6d1edc2
* give shutdown the correct sling.home setting. Add timeout capability to wait_for_startup script. (cherry picked from commit 27d967c33681c0396c0720aa650d42cd635b22cb) 939d479
* adding the integration module I overlooked before (cherry picked from commit ea106c81d4df8e9143597f21c7d87cc2ce8e6f1a) 8dc6589
* add maven project to automate integration testing (cherry picked from commit 6b233511fb28dc069f1d7c042290c10e5ea19c5b) c6c374e
* NOJIRA testing campfire integration e957ce0
* ACAD-65 Users can authenticate with their email address. b5c8845
* ACAD-65 Case-insensitive auth via a Filter and UserFinder 26b3f45

acad-1.1.1
----------
* ACAD-751 Add sparse index fields to build (as opposed to using sparseconfig) 3fddcbd
* ACAD-749 escaped tag names to filter out non-JCR characters that were causing sparseupgrade to fail on tag migration 03a6ba4
* ACAD-715 rev'ed our version of upgrade bundle to reflect our inclusion of a cherry-picked fix for https://jira.sakaiproject.org/browse/SAK
* NOJIRA rev dep version for solr f4e4c80
* NOJIRA merge Erik's fixes for preview processor 612ec7e
* Squashed commit of the following: 02d489f
* KERN-2569 LiteDefaultContextIdResolver missing NPE check (cherry picked from commit 0a93d16452b625bc9f366b39b74d2db5a14d8855) c22f94c
* ACAD-667 KERN-2566 jackrabbit content loader race condition (cherry picked from commit 7c5e6db581e31dd0db53a3651e49ad24d6d2f08f) 4400e36
* ACAD-667 KERN-2566 jackrabbit content loader race condition 14bca74
* ACAD-667 KERN-2566 jackrabbit content loader race condition (cherry picked from commit cc4bd7e0a0c4f9d3548869cc6c117ba5aeeacc5a) e41a6fc
* ACAD-145 fixed some merge isses and case insensitive email addresses cf422ce
* NOJIRA update artifact version number to 1.1.1-SNAPSHOT 07a8681
* ACAD-713 reverted name of sakaiproject app back to community's 435bdce
* ACAD-713 remove nakamura-extensions bundles from app; these will be added by nakamura-extensions/app efa1fed
* ACAD-114 changed version numbers for nakamura-extenions dependencies dffc88f
* ACAD-707 changed refereces to solr and sparse to match rsmart revisions 71146fa
* Squashed commit of the following: e6139b8
* fixed csv process to work with empty fields 4f40132

1.1.0-M1-20120130
-----------------
* NOJIRA release/1.1.0-M1-20120130 7e65741
* NOJIRA fix usage message (cherry picked from commit 38fd866f2d86af4e2bfd4a6bcdc66f8f65260735) fa30807
* KERN-2482 Fix backwards compatibility to ruby 1.8.7 (cherry picked from commit c786146101f18d839f416770cafaa3f599192c5b) 63614d3
* KERN-2462 Converted code.py term extraction service to the ruby library that implements the same algorithm. (cherry picked from commit 6e8
* NOJIRA Fixing up the import scripts - Changed the way row values are trimmed - Removed excess whitespace - Ruby-ified some of the syntax -
* ACAD-554 create provision-worlds.rb - Modified import-base to fix some errors I was seeing in various Ruby versions - Changed the require 
* ACAD-612 blank participant list in Preview course KERN-2434 Handle unfound group member. (cherry picked from commit 10e22a89d9a63bcbdfedf6
* ACAD-653 migrate sakai:category property from 'courses' to 'course' fbeadc6
* ACAD-100 Fix the slideshare API integration a01c184
* ACAD-556 Allow users who were rejected from joining a group make that request again 8a5a792
* ACAD-554 create provision-worlds.rb - Modified import-base to fix some errors I was seeing in various Ruby versions - Changed the require 
* ACAD-273 added some debugging from Erik Froese for failed files 97694b8
* ACAD-273 undo this change as failing content is preventing queue from further processing. cccaef4

acad-1.1.0-M1-20120124
----------------------
* NOJIRA release/acad-1.1.0-M1-20120124 d8e2f6a
* ACAD-623 changed groupId for new rSmart solr bundle dep Evaluate Ray Davis Stopgap for Reindexing Errors 4792f9e
* ACAD-623 Changed dependency to refer to Solr 1.3.1, which fixes reindex rollback issue 089c4dd
* NOJIRA rev'd mysql driver version (no other change) a6c4fd7
* Revert "NOJIRA changed base pom version back to 1.1 per Duffy" 50e2dcb
* NOJIRA changed base pom version back to 1.1 per Duffy 11d2b16
* ACAD-628 Merge in sparse 1.3 with sparse index fix for OIPP 97fb124

acad-1.1.0-M1-20120116
------------------------------
* NOJIRA release/acad-1.1.0-M1-20120116 60f95a9
* ACAD-100 Fix the slideshare API integration 624286d
* ACAD-452 ACAD-553 ACAD-569 fixed various things in the user import process 63b936f
* ACAD-496 corrected bracketing of query after merging changes from usergroups.json d0cd3f9
* ACAD-496 altered search to fit new template syntax 5c13eb9

acad-1.1.0-M1-QA1-20120105
--------------------------
* NOJIRA amended changelog for QA tag pre-acad-1.1.0-M1-20120105 74b3f41
* NOJIRA amended uxloader version number to match 3akai-ux commit 08c9e29ca5f6aaea863313a11cbca0dfcf5c2a3d dea048b
* KERN-2490 KERN-2375 bump the upgrade bundle version number for these bug fixes 52a7589
* KERN-2477: Write a migrator to convert old world data 629a8c3
* KERN-2490 Be even more defensive in case the tag comes up empty after stripping out /tags/ f718d7e
* KERN-2490 Deal with tags that start with / or /tags 123d43e
* KERN-2490 Use JcrUtils.deepGetOrCreateNode so we create intermediate directories in multi-level tags 1a04664
* NOJIRA created pre-acad-1.1.0-M1-010312 tag for QA e6896ef
* NOJIRA fixed pom.xml reference to parent pom; lack of relativePath broke build f86be89
* ACAD-481 minor pom fixes to align to 3akai and 1.3 sparsemap 4a41135
* [maven-release-plugin] prepare release 1.1-RC7 b5c9925
* switching from SNAPSHOT to full version in config files 7705382
* fixed problem with blank row ACAD-477 c19c315
* KERN-2420 a better name for the string validation 66d28b9
* KERN-2420 stop re-encoding params. perform validation on world creation group ID. 0a0aa08
* KERN-2314 update SlingRuby readme and add curb and json to Gemfile 85d932b
* KERN-2464 Prepend /tags/ in preview process before tagging. Allow spaces in terms from extractor. f03c21a
* KERN-2464 Corrected concatenation 869124f
* KERN-2400 must bind to the fixed solr bundle 04c2bb4
* KERN-2464 Add check for requested tags starting with /tags/. Prepend /tags/ to extracted terms so they are sent to the server correctly. e5b4a1f
* KERN-2451 Turn down log level when processing deleted nodes 0e5ae10
* NOJIRA Remove unnecessary ACE setting; fix integration test a5139bb
* KERN-2459 Added service documentation. 51899b8
* KERN-2263 switch to array syntax for POSTing multiple :applyTo params 8e715bf
* KERN-2459 Updated SparseTagOperation to allow multiple tags in 1 request.  Updated unit test to match changes. Changed preview processor to send all tags to tagging operation rather than updating the content directly. Changed check for isAutoTagging to not require the property to exist. 927ab28
* NOJIRA fix broken contrib builds d913591
* KERN-2439 bind to solr-1.3-20111209 067ea84
* KERN-2444 Added check for missing pages before adding to list. c7fbdc8
* KERN-2439 Use 'general' field to search for better results. c43e928
* KERN-2248 protect against null comments on a piece of content. 91fc77f
* KERN-2248 add commentCount to manager-viewer feed 76f7267
* SAKIII-4470 resolve merge conflict 02410c7
* NOJIRA Fixes to poms for ldapauth and samlauth. f7bd07a
* NOJIRA update version number for documentation f1a6864
* KERN-2442 - Content problem in new We've added some tags to your content message 2548b30
* KERN-2388 Updated code to get joinRequestUpdate content once 1ef96a3
* KERN-2420 ensure that _charset_ is on the world create parameter map 44ae0ec
* KERN-2404 added shell script to sanitize old felix config files for a migration aaae98d
* NOJIRA restrict the ianal plugin to the base project f0bd3cb
* KERN-2285 cast a wider net for the poms to process f3a2a58
* KERN-2285 move pre processing out of pom and give processing files better names f82a404
* KERN-2436 Return empty list rather than null. a75efec
* KERN-2434 Handle tags when not an array. a36971f
* KERN-2434 Handle unfound group member. 10e22a8
* KERN-2422 Added 'system' to blacklisted authz. f7af5ca
* KERN-2435 Change String cast to String.valueOf 540367b
* KERN-2430 Adjust acls to set read for anon and everyone. ec9ab58
* SAKIII-4469 have preview processor check if user wants autotags 2a4315d
* KERN-2285 improve the handling of version numbers during release d0ae389
* KERN-2416 Fix broken integration test 61b5e22
* KERN-2416 Integration test to exercise LiteModifyUserAceServlet; small style tweaks c584e07
* KERN-2416 Create servlet to allow updating a user authorizable's ACE 3dbea63
* KERN-2409 Refactored deleted paths cache management to a service. Added handling of path collapsing when a parent path is deleted. Added unit test for sanity. 8f2f087
* KERN-2263 fix port :applyTo capability (multi-group delete) to sparse c7c924a
* KERN-2263 move adminSession initialization to outside the authorizable iteration ad01626
* KERN-2388 Updating user membership check filter fa5017a
* NOJIRA Remove dep that breaks the build in a clean environment. 727d4cc
* KERN-2417 Adjusted ttl to match the ttl of the highest priority queue. This quiets the message of an unsatisfiable ttl. 6fd8b70
* KERN-2413 Really loop over all solr documents c276026
* KERN-2285 change version number scheme to simplify releases 3c654b0
* KERN-2413 Get all tagged docs by going direct to solr server; fix crash when no /tags node exists in jcr 3917673
* KERN-2381 Changed path and message on the event sent when an alternative stream is added. Changed consumer of activity event to not write activity node when the message indicates that an alt stream was created. 24c569d
* KERN-2382 manager-viewer search now respects the levels-of-recursion selector bda0610
* KERN-2388 space added in the code for styling purposes 716acb2
* KERN-2421 Adding a test case for when a browser passes in an abs pathname. 2b2533b
* NOJIRA Fix query servlet to handle multiple values per parameter. d1fb66d
* KERN-2421 Adding scrub for rare browsers that still pass in absolute paths for file uploads. 4891d3c
* KERN-2388: After ignoring a join request, the next join request that user makes throws a 500 2eda15d
* KERN-2411 tweak so that parameter names agree 54593fe
* KERN-2407 Removed result processor since facets do all the work. 3efae81
* KERN-2414 Added check before adding file names to make sure empty names aren't added thus causing weird joining. 1dce41b
* KERN-2398: logout admin's session b8a76bc
* KERN-2398: fix downing Sakai docs error 63a6a2e
* KERN-2411 correctly match message tokens for new world d22d0bc
* KERN-2412 Improve preview_processor logging on backtrace 0285d67
* KERN-2263 allow non-admins to delete an authorizable if they have delete privilege on it 92a8ae2
* KERN-2263 fix intermittent failure of group deletion a642bbd
* KERN-2405 KERN-2408 Allow a content field to be set to 2 solr fields rather than using  copyField since we can't guarantee single values (messages has title and lastName). Removed some duplicated code when indexing messages that was adding an extra ID value (must be single value). d127a7f
* KERN-2354 Implementation - Squashed commit of the following: 284ffe2
* NOJIRA add wagon-ssh extension so deploy will work in Maven 3 2e77f72
* KERN-2401 Added check for field before fetching it. 2b0a32c
* KERN-2267 add metatype=true so we can change configuration 7426237
* KERN-2393 Set the excludeSearch flag on each piece of content created in a world using the data from the template f807bc0
* KERN-2391 Always remove the creator an explicit manager of the content created from the creation of a world a0dddd0
* KERN-2399 CAS Proxy should not create user home 6d385d2
* KERN-2383 Sort on combined 'general' field. 883b5ce
* KERN-2379 Add execute permission 8ed935d
* KERN-2385 Added back storing of tag counts. Centralized common methods to TagUtils. Changed lookup of tags under a directory to use solr rather than walk the content tree thanks to KERN-2396. Added use of existing constants where applicable. 176dec0
* KERN-2385 Moved tags.json and directory.json from 3akai-ux bundle to load via bundle activator into sparse. 48b8d10
* KERN-2385 Changed handling of sparse targetted content to check the entire parent graph for a sparse stored node. Added ability to specify sling:resourceSuperType in the request or as part of a content import. 5d55d66
* KERN-2379 Migration to rename psuedogroupparent to parent-groupid e6e4997
* KERN-2282 integration test to test pseudoGroup property migrator 8c68bec
* KERN-2396 Added indexing handler for tag nodes. 098992f
* KERN-2299 resolve merge conflict, move fields to the correct solr document 623b569
* KERN-2397 add limited abilities for non-managers to update pseudo-groups categorized as collection d831d26
* KERN-2267 fix a problem with NPE on activation 39490de
* KERN-2267 adding some more default configuration properties 2522e4f
* KERN-2267 adapt the CSU patch from ieb to our repository dd227db
* KERN-2273 remove the guards that keep pseudo-groups from being considered as levels 90bf1c4
* KERN-2365 Fix errant POMs after latest Solr merge 0fbb339
* KERN-2379 fix constants used by meservice. unbreak the build. 24f940f
* KERN-2295: Ignoring non-IMSCP files instead of throwing exceptions 0619612
* KERN-2295: Ignoring non-IMSCP files instead of throwing exceptions d3d9e29
* NOJIRA Changing some world properties around for pseudoGroups to make it easier on the UI * Note: this requires a migration -- we need to migrate sakai:pseudogroupparent -> sakai:parent-group-id * The UI never used this property, as it was never returned with the pseudo group, so this has no risk for the UI to put in right now * Everything else is just for the worlds servlet, which hasn't yet been pushed into the UI master 132dc6b
* KERN-2365 Cleaned up toss-out code from immediate indexing reversion. Changed ttl to 1 as >= 0 gets turned to Integer.MAX_VALUE. Updated to solr 1.3-SNAPSHOT to match solr repo. Added some comments & javadoc. ee4e17c
* KERN-2365 switch to solr 1.2 and sparsemapcontent 1.3. Fix build errors. cedfce2
* KERN-2352 Corrected cast to string with .toString() so LongString doesn't cause ClassCastException. caba361
* KERN-2350 Added forced state of 'ACCEPTED' when searching others' contacts. 64030d2
* KERN-2350 Added parameter ("user") to search another user's contacts. 94d0907
* KERN-2376 Changed toString to getString to retrieve the value correctly. f5d9f06
* KERN-2374 Move variable inside block where it's used 9dcd4d6
* KERN-2374 Make the TagMatch property provider deal correctly with q=* 41765dd
* KERN-2363 Fix unit test 10fa001
* KERN-2373 Added JcrUtils.isJCRProperty library method b9f879b
* KERN-1536 add protections for editing non-existing comment and editing comment with insufficient permission c2218f1
* KERN-1536 enable properties to be set and edited on a content comment 4081178
* KERN-2214 Removed properties -> jsonobject -> map conversion to go from node to map directly. 0080965
* KERN-2363 Moved default values to the sakai:query-template-defaults block. Adjusted SolrSearchServlet to use this defaults block. Corrected how a property provider finds the resourceType. f558b2d
* KERN-2348 fix path matching problem that caused replies not to be written b58c112
* KERN-2325 add back stuartf change, which seems to work on further testing 53ac0b6
* KERN-2277 Make kern-2277.rb executable 1631300
* KERN-2359 add 'homePath' property to basic user info 41823cd
* KERN-2342 update javadoc to match behavior fbfc956
* KERN-2266 Add tag counts to more searches, including the Me service, where they are faked up to resemble the format that Solr gives us d2b3777
* KERN-2353 Removed use of tag uuid in searches. Changed search templates to look for tags directly or set a default of "" when using the TagMatch property provider. Changed tagcloud to use facets and return no search results. Corrected the logger used by MimeMatch.. d5eba9d
* KERN-2342 Make ExtendedJsonWriter properly handle empty arrays bd9e8d9
* KERN-2368 Add mincount=1 so Solr won't show 0-count tags that aren't represented in the result set b0cfb94
* KERN-2367 Changed use of "tag" to "tagname" for facets. 60a1e6a
* KERN-2277 Introduce new :editors role with Read and Write permissions 132139b
* KERN-2366 Cleared cache when activate/modified is called. Changed local use of Tika detector to use the TikaService. Collapsed activate, modified, init methods into one since they were all chained and not different. Split TikaService into api/impl for easier testing. Should've done that when I created it. b0886f1
* NOJIRA don't load tests from the target directory 7ed268c
* KERN-2364 Remove the world templates from world bundle since the UX will be loading its own; update world integration test so it uses its own self-contained template a8361c4
* KERN-2360 Since the Sling i18n bundle has been removed, needed to rework the query servlet to use a different i18n mechanism. 014b798
* KERN-2358 bind to latest sparsemapcontent 100d4db
* KERN-2343 Corrected path for messagestore when searching for messages. Reverted previous changes to remove trailing slash. d87f2a7
* NOJIRA Remove unused i18n sling bundle. b0d8e77
* KERN-2352 Safely handle indexing of LongString SakaiDoc pages 317921e
* Revert "KERN-2325 remove unneccessary/broken extensions specification" f71c722
* KERN-2325 remove unneccessary/broken extensions specification c931a5c
* KERN-2343 Remove unused import 38a2fb7
* KERN-2343 Use endsWith and substring to trim trailing slash instead of regex 597d2e7
* KERN-2343 Strip trailing slash from path to match what's stored in Solr index 0e2e19a
* KERN-2325 fix stripping leading char from file extension e8e1575
* NOJIRA clean up trailing spaces and ignore compiled python 48872ab
* KERN-2274 With new tagging system, no need to create tag before assigning it 23d9c77
* KERN-2336 Updated form auth redirect parameter to match other authentication handlers and the UI. 66ea490
* KERN-2351 Removed use of local config since we modify the mimetypes file and put it back where tika looks for it (tika doesn't really know we changed it). By using the default config, tika loads parsers using a classloader lookup which was bypassed by introducing our own config unless we add the parsers to the config. 643ede9
* KERN-2310 Don't throw a server error when a contact profile isn't visible fd60944
* KERN-2309 Don't explode when a commenter's profile isn't visible 605f2af
* KERN-2312 Inline unnecessary variable a960a8d
* KERN-2312 Since we may not be able to read authorizables or content nodes, log AccessDeniedExceptions as debug instead of warn level 749398a
* KERN-2437 add code to respect the sakai:excludeSearch property when indexing pooled content 7f6b2ef
* KERN-2349 correctly calculate offset due to daylight savings. b440809
* KERN-2302 allow iteration to continue when we encounter a group member we're not allowed to see d7d7e1c
* KERN-2273 add support for controlling the number of levels to traverse in pooled content search. 3dec4a0
* KERN-2318 Moving context id resolving code into the actual context id resolver. a281d0a
* KERN-2344 Add some additional CLE proxy matches to hybrid apache config 81c5986
* KERN-2274 Update world templates to new structure same as simple-group.json db3a3e4
* KERN-2279 Removed use of id field. Tag references will be managed using the path. Updated tests and corrected a few to fail appropriately. 5b1b734
* KERN-2279 squashed commits for pull request f513a4e
* Squashed commit of the following: 69c1c8f
* KERN-2336 update unit test to match changed parameter 8daa062
* SAKIII-2318 Checking the launch URL for an optional Group ID.  If there is a Group ID, looking it up and checking to see what permissions the current user has in it, and using the Group as the BLTI context ID. If the node as the optional sakai:cle-site property, and the launch tool is a sakai 2 tool, then we use the cle-site property as the BLTI Context. 9e54e18
* KERN-2336 change "d" parameter to "url" to match ux ae9f97c
* KERN-2332 add unit tests to cover all the rules about how content members are updated 9edcff1
* SAKIII-4262: support edit imscp file && export imscp with namespace 32c9810
* NOJIRA Don't hit the full profile service from the ProfileNodeSearchResultProcessor.  Use the BasicUserInfoService instead. ac5c69f
* KERN-2270 responding to Ray's concerns e2c6556
* Squashed commit of the following: 87e41d7
* KERN-2337 cut out some code you didn't end up needing 3508bbb
* KERN-2337 add a missing return statement 2ac91dc
* KERN-2337 rewrite the POST method so it is coherent and precise (and not broken) bfd5c08
* KERN-2334 404 when trying to download content 51d479c
* KERN-2331 Removed classes that were replaced by lite rewrites. Updated tests to keep coverage. Fixed any breakages in the build. b1281df
* Create CasProxyServlet a658a5b
* KERN-2313 Do not throw NullPointerException on an empty page node f92de81
* KERN-2327 Deal softly with unrecognized CAS users. Remove obsolete code. b5ad4a6
* KERN-2281 Fix casauth for latest dependency changes 83c9776
* NOJIRA Fix CAS POM error which slipped by before because of looser dependency scope. 2843a48
* KERN-2196 Sleep 10s whenever config changes, even after teardown, so that the stack can restart and subsequent tests aren't affected 33baf6a
* NOJIRA fixed missing dependencies ce52e66
* KERN-2323 fix bundle category 93193df
* KERN-2281 Upgraded managed dependencies and plugins. Updated code to fix build and reduce usage of deprecated methods/classes. Cleaned up some files for easier reading, reduce cruft. Added README.txt to server and tika bundle to note what is needed to upgrade those bundles. Added new tika-config.xml to reflect tika upgrade changes. Corrected code and unit test associated to getting pooled content. 99da1ea
* KERN-2281 Upgraded bundles in list.xml to match the latest list.xml from Sling. df347b9
* KERN-2267 list the SakaiStickyPolicy within the load balancer Listener 04a0022
* KERN-2267 add load balancer stickiness policy 8193e16
* KERN-2267 create initial CloudFormation template for an OAE cluster 62c4099
* KERN-2317 Adding sakai:group-joinable to group properties in BasicUserInfoService. a0c92d2
* KERN-2316 add rubygems as a dependency for runalltests.rb ebbc32d
* KERN-2305 Modified /var/search/pool/manager-viewer.json to honour the 'mimetype' parameter. e71d77d
* KERN-2280 get the mvn site goal up and running again 1bbc96a
* NOJIRA removing redundant declarations of pom.xml f75af65
* KERN-2111 KERN-2219 Added interface after final changes in solr separated 1 interface into 2. 87c6787
* KERN-2275 - fixing annotations and metadata, adding integration test 4976ec3
* KERN-2301 add a unit test to confirm that managers can remove viewers bd85057
* KERN-2301 remove restriction on content managers removing viewers dcdaf41
* NOJIRA add lifecycle mappings for m2eclipse 60758f2
* KERN-2266 Tags need to be represented as array of objects so that order is preserved 80bdca9
* NOJIRA - fixing executable flags on 3 integration test scripts 4f7121b
* Squashed commit of the following: 93739b3
* KERN-2281 upgrade sling.commons.scheduler, mainly to fix those unsightly Maven warnings about quartz. b615f9e
* KERN-2281 change to a not-so-new maven-bundle-plugin, because it threw a wrench in the proxy bundle f61bb41
* KERN-2294 change the links for javadoc APIs e692988
* KERN-2294 update build to target Java 6 a9118e9
* KERN-2281 update our base pom with up-to-date versions of maven plugins 90e7936
* Allow user to specify number of times to run e14112e
* KERN-2111 KERN-2219 Added index handler for immediate processing. Corrected booleans. f0e61f2
* KERN-2111 KERN-2219 Changed 3 indexing handlers to work with immediate indexing. Updated queries to use solr instead of sparse. d9d6877
* KERN-2210 - adding use of ConnectionManager to ProfileServlet and added integration test for authprofile feed 6390817
* NOJIRA Added metatype=true to service. Minor formatting cf3aeee
* KERN-2095 Have RequestInfo parse URL parameters if supplied. 457d777
* NOJIRA make preview_processor run as a daemon c8f9567
* KERN-2292 fix artifact id of files.impl bundle. 7294a50
* KERN-2292 update list.xml to use the two new bundles 7edb790
* KERN-2292 create an API and impl bundle for the files module, permitting hot redeploy 76b6cf1
* NOJIRA Updated refs to 1.1-SNAPSHOT from 0.11-SNAPSHOT 6a51fa9
* KERN-2290 Removed local httpclient bundle and added servicemix's bundle. Changed ref in captcha bundle to point to httpclient dep. da20f50
* fix problems of OSGI property 9bd6363
* KERN-2254 - fixing the issues Carl documented in pull request comments 38bf95c
* KERN-2254 squash commit of search and bundle splitting after merge with master 1ccf9a0
* NOJIRA removing redundant dependency declaration for commons.json 0bcdf5d
* using OSGI property to config isHierarchical 103e6ad
* KERN-2268 Added specific package to export to clear build warning. 5c672ff
* NOJIRA updating the version number of the nakamura gem we use. 32fad36
* KERN-2265: support 2 types of IMS-CP import f2790bd
* KERN-2268 Set managed dependencies to provided. Centralized some deps that were missed in previous work. Cleaned up dependencies in modules. bb8b473
* KERN-2271 The JSON Wrapper value function only seems to create a list if it's a java primitive [] type list rather than a List object. c23325c
* NOJIRA fix tests that aren't using the gem b4f93a5
* NOJIRA Change nakamura version number for csu build in contrib/csu bb9c152
* NOJIRA Correct nakamura version number for oracle 11g driver in contrib/oracle-jdbc-6 52bcb70
* NOJIRA Change nakamura version number for environment variable K2VERSION in tools directory 77c75da
* KERN-2147 If reindexAll is true, also reindex all content and authorizables in the Solr indexes 32fd06e
* NOJIRA add a gemfile to track ruby dependencies bcd278c
* KERN-2147 Document the upgrade servlet 1347644
* KERN-2147 and KERN-2242 - Create an upgrade bundle, with its first upgrade, which converts pseudoGroup to a true Boolean 3287377
* KERN-1920 reopened - set Jetty form field limit to 2 gig 0929da3
* KERN-2255 Added tls, ssl booleans and authentication user/pass configurable fields. Added metatype information for the console. Cleaned up some imports. 031ec09
* KERN-2210 - squash commit adding connectionManger to ProfileNodeSearchResultProcessor for use by groupmemebers seach template and removed that logic from subclass ProfileContactsSearchResultProcessor where it would be redundant.  Added int test script for users, groupmembers and general feeds. Authprofile feed addition on hold because adding connectionManager to ProfileServlet that handles that feed creates a circular dependency 1db22c2
* NOJIRA Updated script to use REST rather than telnet and to output some basic stats. 65ff0f2
* KERN-2252 Remove obsolete Foreign Principal support 297b51c
* KERN-2223 Added handling of RelayState to/from SSO server. c0fa596
* KERN-2244 Centralized sling dependencies and set version numbers to the highest being used. Updated some resource classes for updated apis. Updated versions to match list.xml 379e1bd
* KERN-2243 Add integration test to exercise all the TypeHints we support; use object wrappers instead of primitives (requires fix from KERN-2246 aka sparsemap #101) 0f1a17c
* KERN-2241 use maintenance mode on the AuthorizableManager to keep last_modified from being updated 49af93c
* use the nakamura gem 8884c4c
* NOJIRA correct the version of the solr bundle we use for 1.1 715e526
* KERN-2243 ModifyOperation now supports TypeHints for Integers, and does not advertise types it does not support; User bundle servlets now support same TypeHints as ModifyOperation 0ebd78d
* KERN-2211-squash fixing executable flag on int test script a493034
* KERN-1938 upgrading from google-collections to guava r09 b31ba1a
* KERN-2209 - explicitly set response character encoding and mimetype - as this servlet returns json b98512a
* Forgot push exporting sakaidoc as imscp 2576ca9
* KERN-2211 - version working with unit and integration tests a6a5df3
* KERN-2232 Allow "Content" and "All" drop down list items to work in solr webconsole plugin. Allow text to replace label "label_reindex_entity". 94adaae
* fix sending message bugs b5f66d2
* Revert "Add content packaging test example" 4de9655
* KERN-2228: auto tagging for uploaded contents 0d59a29
* Add content packaging test example a56b4d3
* remove some useless code 2fb07ac
* add IMS CP support for sakai 60ea3aa
* KERN-2221 Remove the Solr index record for the deleted user or group, not for the user doing the deletion 1b0e758
* restoring the core dependencies that need to be explicit 53ff0bd
* KERN-2220 Centralized solr refs to dep mgmt. Removed test class that was failing with new solr jars. Adjust packages references from queryParser to queryparser.classic. Added o.s.n.core to poms where needed for the build. Replaced solr-solrj references with o.s.n.solr refs. 103a726
* KERN-2218 SSO authentication using SAML Merged patch from Stephen Crow <stephen@hallwaytech.com> for initial OpenSAML impl. Renamed OpenSSO project to SAML to better match the functionality impl'd. fefb0b5
* KERN-2216 Adding a basic graphviz python script to draw our bundle dependencies. e9e442d
* KERN-2188 Rewrote migrators to use PropertyMigrator inteface. 1077f29
* KERN-2194 Added escaping for deleted paths and current user ID. 00aef71
* KERN-2213 Added ability to have multi-value fields in the search options. Adjusted filter queries to use multiple values where appropriate. Adjusted supporting classes of the changed queries. 8fcbe84
* KERN-2212 Was able to remove the AND category clause because the UI doesn't use it and reduce this query from ~29 terms to ~13 removing lots of repeated field searches. 04ddf20
* KERN-2107 Add processing for multiple filter queries. Changed deleted path terms to filter query. a38075c
* KERN-2196 Ruby and JMeter tests to exercise long string property handling 33d1558
* KERN-2107 Move "reader restrictions" from solr "q" parameter to solr "fq" parameter so that enforcing "reader restrictions" does not affect the solr score of a document. 5baca82
* KERN-2206 Changed value to Object from String. Updated code that used this. Added processing to handle multivalue parameters. 8aaa074
* Squashed commit of the following: 603f815
* KERN-2191 Added bundle that adds a web console plugin to solr allowing for querying and reindexing. 71117e4
* KERN-2157 Added help output. Added form for searching without having to keep up with the basic query params. Added full output indenting. Abstracted processing to service for use by other processes. Put html into templates with language bundle. Added dependency to sling.i18n for resource bundle in JCR resolution. Put the help text into a static file. Updated the template service to allow a reader as param. 7075f82
* KERN-2200 Upgrade mysql-connector-java to latest version 5.1.17 664b02f
* KERN-2199 Include /dav in reference hybrid apache httpd configuration aba274a
* NOJIRA Escape the = sign in the regex for better syntax highlighting This has no functional effect: $ irb ruby-1.9.2-p0 > str = "1=2"  => "1=2" ruby-1.9.2-p0 > str.split(/=/)[0]  => "1" ruby-1.9.2-p0 > str.split(/\=/)[0]  => "1" d56fa1a
* NOJIRA Create Time.to_nsec to make time conversion to nanoseconds easier $ irb ruby-1.9.2-p0 > now = Time.now  => 2011-08-26 12:06:14 -0700 ruby-1.9.2-p0 > now.to_f.to_s.gsub(".", "")  => "1314385574949142" ruby-1.9.2-p0 > now.to_nsec  => 1314385574949142 64defcb
* KERN-2122 - added rep:group-managers to AuthorizableIndexingHandler group properties to be indexed by solr in manager field, changed solr search in ManageMembersContentPoolServlet.findMyManageGroups to seach manager field directly, cleaned up kern-2122.rb int test cf5d6d9
* KERN-2122 - fixing continuing whitespace issue with list.xml 81d9797
* KERN-2122 - fixing executable flag on kern-2122.rb and continuing whitespace issue with list.xml 3528bf2
* KERN-2122 - fixing kern-2122.rb reference to full_group_creator.rb and first working version of removing content from managed groups eaaa35c
* KERN-2198 - moved full_group_creator.rb into lib/sling and made file_log optional 5748b09
* KERN-2185 Added group filter to tag cloud with unit test. 93ceea1
* KERN-2122 - added logic to allow a user to remove a content item, that they do not own and don't have write access to, from My library.  They would do this by removing themselves from a content item's viewer list via a :viewer@Delete= POST to the content item's URL appended w members.json.  A typical curl statement would be curl -e /dev/testing -u bp7742:testuser -F:viewer@Delete=bp7742 -F_charset_=utf-8 http://localhost:8080/p/iOkuc2Kv38.members.json.  As the user does not own the content item, they can only remove themself from the viewer list. 04d418e
* NOJIRA - Adding proxy node for ted talks 53049f6
* KERN-2218 SSO authentication using SAML Merged patch from Stephen Crow <stephen@hallwaytech.com> for initial OpenSAML impl. Renamed OpenSSO project to SAML to better match the functionality impl'd. c28cf1d
* KERN-2216 Adding a basic graphviz python script to draw our bundle dependencies. bf3c74d
* KERN-2188 Rewrote migrators to use PropertyMigrator inteface. 68e2cd5
* KERN-2194 Added escaping for deleted paths and current user ID. a39ecf0
* KERN-2213 Added ability to have multi-value fields in the search options. Adjusted filter queries to use multiple values where appropriate. Adjusted supporting classes of the changed queries. 42d00f4
* KERN-2212 Was able to remove the AND category clause because the UI doesn't use it and reduce this query from ~29 terms to ~13 removing lots of repeated field searches. 2a9853f
* KERN-2107 Add processing for multiple filter queries. Changed deleted path terms to filter query. d11f086
* KERN-2196 Ruby and JMeter tests to exercise long string property handling c7c9f4f
* KERN-2107 Move "reader restrictions" from solr "q" parameter to solr "fq" parameter so that enforcing "reader restrictions" does not affect the solr score of a document. 53d1a38
* KERN-2206 Changed value to Object from String. Updated code that used this. Added processing to handle multivalue parameters. 1589640
* Squashed commit of the following: 0f7ec20
* KERN-2191 Added bundle that adds a web console plugin to solr allowing for querying and reindexing. 25553b2
* KERN-2157 Added help output. Added form for searching without having to keep up with the basic query params. Added full output indenting. Abstracted processing to service for use by other processes. Put html into templates with language bundle. Added dependency to sling.i18n for resource bundle in JCR resolution. Put the help text into a static file. Updated the template service to allow a reader as param. 0eb48ce
* KERN-2200 Upgrade mysql-connector-java to latest version 5.1.17 554d863
* KERN-2199 Include /dav in reference hybrid apache httpd configuration ebce004
* NOJIRA Escape the = sign in the regex for better syntax highlighting This has no functional effect: $ irb ruby-1.9.2-p0 > str = "1=2"  => "1=2" ruby-1.9.2-p0 > str.split(/=/)[0]  => "1" ruby-1.9.2-p0 > str.split(/\=/)[0]  => "1" 29c6157
* NOJIRA Create Time.to_nsec to make time conversion to nanoseconds easier $ irb ruby-1.9.2-p0 > now = Time.now  => 2011-08-26 12:06:14 -0700 ruby-1.9.2-p0 > now.to_f.to_s.gsub(".", "")  => "1314385574949142" ruby-1.9.2-p0 > now.to_nsec  => 1314385574949142 58ad524
* KERN-2122 - added rep:group-managers to AuthorizableIndexingHandler group properties to be indexed by solr in manager field, changed solr search in ManageMembersContentPoolServlet.findMyManageGroups to seach manager field directly, cleaned up kern-2122.rb int test a314a64
* KERN-2122 - fixing continuing whitespace issue with list.xml f62a859
* KERN-2122 - fixing executable flag on kern-2122.rb and continuing whitespace issue with list.xml 01306ea
* KERN-2122 - fixing kern-2122.rb reference to full_group_creator.rb and first working version of removing content from managed groups 17378bd
* KERN-2198 - moved full_group_creator.rb into lib/sling and made file_log optional 322d77e
* KERN-2185 Added group filter to tag cloud with unit test. 65bf185
* KERN-2122 - added logic to allow a user to remove a content item, that they do not own and don't have write access to, from My library.  They would do this by removing themselves from a content item's viewer list via a :viewer@Delete= POST to the content item's URL appended w members.json.  A typical curl statement would be curl -e /dev/testing -u bp7742:testuser -F:viewer@Delete=bp7742 -F_charset_=utf-8 http://localhost:8080/p/iOkuc2Kv38.members.json.  As the user does not own the content item, they can only remove themself from the viewer list. 1beae14
* NOJIRA - Adding proxy node for ted talks 7990d3f
* NOJIRA - Adding proxy node for ted talks 58678a4

acad-1.0.2-M5-20111215
----------------------
* No changes

acad-1.0.2-M4-20111208
----------------------

* NOJIRA ignore script/.contentpreview.success file from preview processor 2151c1e
* ACAD-273 Disable preview processor failures until we have processor working as expected. 3cf26fb
* KERN-2412 Improve preview_processor logging on backtrace 69b828e

acad-1.0.2-M3-20111202
----------------------

* NOJIRA prepare for 1.0.2-M3 9f3d2ce
* added ability to skip first row 72bac2f

acad-1.0.2-M2-20111116
----------------------

* ACAD-168 corrected commons-lang version in appliation launcher a77b47e
* NOJIRA updated artifact versions for 1.0.2-M2 bff132f
* ACAD-168 fixed pom version errors 899582e
* ACAD-168 tagging in sparse. squashed commits; these have been cherry-picked from sakaiproject/master, or are minor mods to compensate for 
* ACAD-195 Performance issues when loading the participants widget for groups with more than 25 members b46ff9f
* KERN-2270 responding to Ray's concerns (cherry picked from commit e2c65567156fc2b26dc7d5e12946c60758e2a6af) 8c77914
* ACAD-113 ACAD-144 Upgrade to sparsemapcontent 1.2 / solr 1.1 f15e32c
* added remove users 20334c5
* added remove users d048d07
* added directions for the nakamura-ruby gem ea70abd
* added cle_userType for the userType field b36ec65
* added properties and email report b5a3ef2
* testing a commit 04e4959
* adding exception handling and report to class eeacce9
* adding new import user process 92b23ff
* added remove users a190157
* added remove users 4884e2b
* added directions for the nakamura-ruby gem 7058457
* ACAD-174 KERN-2334 404 when trying to download content b588314
* added cle_userType for the userType field e374dcd
* added properties and email report 6a8e7d0
* KERN-2211 - version working with unit and integration tests (cherry picked from commit a6a5df3f4ae3715e5b35f94a024fdd1daf3f320c) 0412f1d
* testing a commit e30158b
* adding exception handling and report to class 679f832
* adding new import user process bed66c2
* Fixed unit test, With the previous change, if runnign http and https on the same host at the same time, all redirects must go to the https
* Allow protection on default ports whem the browser is passing headers with no port.(cherry picked from commit f27139f21b0550b416293bde8656
* fixed version of mysqljdbc c6d5341
* fixed the path to point to relative parent 8e86310
* ACAD-122 Non-resolvable parent POM: Could not find artifact org.sakaiproject.nakamura:base:pom:1.0.2 7186c78
* ACAD-115 Implement new ServerProtectionService 2214ec3
* NOJIRA switched mysql jdbc driver bundle from 1.0.0 to 1.0.2 to fix build errors f3a9a60
* NOJIRA change app version to SNAPSHOT and rename mysql jdbc bundle name 1cd81d7
* ACAD-103 Enable postgres jdbc driver 839e074
* ACAD-103 Enable postgres jdbc driver 5c0c86a
* NOJIRA Added CHANGELOG f977936

acad-1.0.2-M1-20111024
----------------------

* NOJIRA Correct version number for oracle 11g driver in contrib/oracle-jdbc-6 529552a
* correcting the host path for the Nakamura web start 5e0d9c0
* changing version numbers for the 1.0.2 release 7e45b12
* OAE-84 Rename unloader artifact to: com.rsmart.nakamura.uxloader a62d452
* updating version numbers for release 66473e6
* NOJIRA changing amazon provisioning so it can be done from Jenkins 4ec8f69
* KERN-2259 update solr version to 1.0.1 and consolidate in the base pom.xml 945663b
* KERN-2249 implement license plugin, add/update headers c373cf7
* KERN-2239 update basiclti test to expect an empty response 32d530c
* NOJIRA add newlines at the end of these scripts 020e319
* NOJIRA creating scripts that we'll use for automating our release pipeline 5e228c4
* KERN-2151 Dynamically create hybrid json config files. 4baf74e
* KERN-2151 Dynamically create hybrid json config files. 4743e94
* KERN-2151 Dynamically create hybrid json config files. 3a6b33f
* KERN-2151 Dynamically create hybrid json config files. 5c3f761
* NOJIRA switching to a datestamp version of sparsemap for stability in the release window of 1.0.1 a902f2a
* NOJIRA consolidating the commons.json version in the base pom be95a88
* NOJIRA use consistent version numbers for org.apache.sling.commons.json 5601d98
* KERN-2180 changing the test to reflect how activity feed permissions work 5867be4
* KERN-2235 - added saksi:pseudoGroup=true to create_pseudo_group() f62948a
* KERN-2230 for hybrid, add /s2site to the list of trusted exact paths 5cf31bc
* KERN-2205 - fixing test failures with wait_for_indexer, NPE with NPE guard and made int test script executable, fixing merge conflicts ce29dd0
* KERN-2163 added unit tests for LiteAllActivitiesResultProcessor f8cd9e0
* KERN-2205-squash - add code to prevent infinite loop in recursion through pseudo-groups, correcting misshandling of updateIntervalMinutes, moving full_group_creator to lib/sling for use in integration test, fixing several constants references to use UserConstants a757530
* KERN-2179 clean up error handling. Allow visibility to underlying exception when necessary. 596f1a3
* KERN-2179 create page indexing utility which works in conjunction with PoolContentResourceTypeHandler 9fd4059
* KERN-2151 remove redundant .json files 5100d27
* KERN-2151 re-added needed statics, added ability to configure list e52b03f
* Removed unneeded 'static' from variable definitions d09d062
* KERN-2151 fixed code review issues, made everything configurable c4fbeb4
* KERN-2221 Remove the Solr index record for the deleted user or group, not for the user doing the deletion ca8b979
* KERN-2151 add servlet to generate json for cle tool basiclti 5be43d7
* KERN-2226 Update Nakamura version on deployment of new code to existing server 5be5690
* KERN-2225 Add additional mime types for preview_processor to processor, and prevent preview_processor from trying to process audio/mpeg mime type. 034d4ba
* change the version number of the solr bundle back to 1.0 b704a5d
* updating the version file to 1.1-SNAPSHOT c6daa27
* switched version numbers to 1.1-SNAPSHOT f27b940
* NOJIRA binding to sparsemapcontent 1.1-SNAPSHOT and centralizing the version number 198876c
* NOJIRA Updated bundles to use 1.1-SNAPSHOT of sparse 1fb4c5c

oae-v1.0.0-M4-20111006
----------------------

* NO CHANGES

oae-v1.0.0-M3-20111004
----------------------

* KERN-2151 Dynamically create hybrid json config files. 1a247b0
* KERN-2151 Dynamically create hybrid json config files. (cherry picked from commit 4baf74efbfc0534ce4b774ff0d3db33ccdf2317c) 6986f4e
* KERN-2151 Dynamically create hybrid json config files. (cherry picked from commit 4743e945894cc3f602e211462984c1c4912dd42c) 48f76fe
* KERN-2151 Dynamically create hybrid json config files. (cherry picked from commit 3a6b33f08eb34036306a5817bae0c3e52958bfe3) 608e6fd
* KERN-2151 Dynamically create hybrid json config files. (cherry picked from commit 5c3f761b70fea98ba50996e0fc900efcac3f8773) b757521
* KERN-2151 remove redundant .json files (cherry picked from commit 5100d2750427506261fb9bd16b1a034a59679b69) cc18bd1
* KERN-2151 re-added needed statics, added ability to configure list (cherry picked from commit e52b03facc83f112778071528a8b9f12a6e5fda5) af36249
* KERN-2151 fixed code review issues, made everything configurable (cherry picked from commit c4fbeb4dde9703fa8dc50901bb95f251eb1424c7) fd41bf9
* KERN-2151 add servlet to generate json for cle tool basiclti (cherry picked from commit 5be43d7afd7fa3d7d0053f6d8c75a9fb1e3c4365) 64280bb
* OAE-33 replaced context-type of CourseSection with course; and user role of Learner with Student 7662fbc
* OAE-47 now using non-Administrative sessions for pulling user contact and group information 58d2cab
* OAE-49 Change artifactId for executable jar to be com.rsmart.academic.app e5095d1
* OAE-3, OAE-29 1c265e0
* OAE-3, OAE-29 22ee8a0
* OAE-29 cfcd350

oae-v1.0.0-M2-20110927
----------------------

* NOJIRA configure LTI client for DEV 97433a4
* OAE-23 14f2be3
* OAE-23 new query file and property provider for a user's contacts b0bae54

oae-v1.0.0-M1-20110921
----------------------

* OAE-3 ae0777b
* OAE-8 Enable MySQL Connector by default cf4fc06
* OAE-3 cc87cf3
* KERN-2221 Remove the Solr index record for the deleted user or group, not for the user doing the deletion (cherry picked from commit ca8b979d34362c853dcec0d6c6f2c4ddd33eaac9) 540e5f9
* KERN-2226 Update Nakamura version on deployment of new code to existing server (cherry picked from commit 5be56904ce987877506db815639b3fce6ae42d09) 01317de
* KERN-2225 Add additional mime types for preview_processor to processor, and prevent preview_processor from trying to process audio/mpeg mime type. (cherry picked from commit 034d4ba90510cca2740cbb483fe579ca0a5e15cc) 0a795f1
* KERN-2199 Include /dav in reference hybrid apache httpd configuration (cherry picked from commit b957c4d0cd810b3737a33a024b68fb967fdbc409) 0957d17
* KERN-2200 Upgrade mysql-connector-java to latest version 5.1.17 (cherry picked from commit c73772aaf902ed0d17c9e3c23b8e882141de0444) fc8b8df
* Inverted registration of LiteAuthorizablePostProcessors to avoid race conditions where dependencies load in the wrong order. (cherry picked from commit 475aaaccf5765aa2dea9c0da1d8191e3695ca81d) 36505ee
* Added missing reference that was preventing custom APP to be registered, may need to invert this. (cherry picked from commit 30ab4458cf1d8159f17aa0c26d7e0e373c522416) 00555ab


