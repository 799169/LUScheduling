package org.learningu.scheduling;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;

public final class RoomProperty extends ProgramObject<Serial.RoomProperty> {

  RoomProperty(Program program, org.learningu.scheduling.Serial.RoomProperty serial) {
    super(program, serial);
  }

  @Override
  public int getId() {
    return serial.getPropertyId();
  }

  static Function<Serial.RoomProperty, RoomProperty> programWrapper(final Program program) {
    checkNotNull(program);
    return new Function<Serial.RoomProperty, RoomProperty>() {
      @Override
      public RoomProperty apply(org.learningu.scheduling.Serial.RoomProperty input) {
        return new RoomProperty(program, input);
      }
    };
  }
}