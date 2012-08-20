package org.sakaiproject.nakamura.media.kaltura;

import org.sakaiproject.nakamura.api.media.MediaStatus;

import com.kaltura.client.enums.KalturaEntryStatus;

public class KalturaMediaStatus implements MediaStatus {
  
  private KalturaEntryStatus kalturaStatus;

  public KalturaMediaStatus() { }

  public KalturaMediaStatus(KalturaEntryStatus kalturaStatus) {
    super();
    this.kalturaStatus = kalturaStatus;
  }

  @Override
  public boolean isReady() {
    return kalturaStatus != null && kalturaStatus == KalturaEntryStatus.READY;
  }

  @Override
  public boolean isProcessing() {
    return kalturaStatus != null && 
        (kalturaStatus == KalturaEntryStatus.PENDING
        || kalturaStatus == KalturaEntryStatus.PRECONVERT);
  }

  @Override
  public boolean isError() {
    return kalturaStatus != null && 
        (kalturaStatus == KalturaEntryStatus.BLOCKED
        || kalturaStatus == KalturaEntryStatus.ERROR_CONVERTING
        || kalturaStatus == KalturaEntryStatus.ERROR_IMPORTING
        || kalturaStatus == KalturaEntryStatus.INFECTED);
  }
}
