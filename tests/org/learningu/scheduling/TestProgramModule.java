package org.learningu.scheduling;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.learningu.scheduling.graph.ClassPeriod;
import org.learningu.scheduling.graph.Course;
import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.ProgramCacheFlags;
import org.learningu.scheduling.graph.Room;
import org.learningu.scheduling.graph.Serial.SerialCourse;
import org.learningu.scheduling.graph.Serial.SerialPeriod;
import org.learningu.scheduling.graph.Serial.SerialProgram;
import org.learningu.scheduling.graph.Serial.SerialRoom;
import org.learningu.scheduling.graph.Serial.SerialTeacher;
import org.learningu.scheduling.graph.Serial.SerialTimeBlock;
import org.learningu.scheduling.graph.Teacher;
import org.learningu.scheduling.graph.TimeBlock;
import org.learningu.scheduling.logic.ScheduleLogicModule;
import org.learningu.scheduling.util.CommandLineModule;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Names;

public class TestProgramModule extends AbstractModule {

  public static Injector bindProgramObjects(Module... modules) {
    return bindProgramObjects(Guice.createInjector(modules));
  }

  public static Injector bindProgramObjects(Injector inj) {
    final Program program = inj.getInstance(Program.class);
    return inj.createChildInjector(new AbstractModule() {

      @Override
      protected void configure() {
        for (Teacher teacher : program.getTeachers()) {
          bind(Teacher.class).annotatedWith(Names.named(teacher.getName())).toInstance(teacher);
        }
        for (TimeBlock block : program.getTimeBlocks()) {
          bind(TimeBlock.class).annotatedWith(Names.named(block.getDescription())).toInstance(
              block);
        }
        for (Course course : program.getCourses()) {
          bind(Course.class).annotatedWith(Names.named(course.getTitle())).toInstance(course);
        }
        for (Room room : program.getRooms()) {
          bind(Room.class).annotatedWith(Names.named(room.getName())).toInstance(room);
        }
        for (ClassPeriod period : program.getPeriods()) {
          bind(ClassPeriod.class).annotatedWith(Names.named(period.getDescription())).toInstance(
              period);
        }
      }
    });
  }

  @Override
  protected void configure() {
    try {
      install(CommandLineModule.create(
          new String[0],
          ProgramCacheFlags.class,
          ScheduleLogicModule.class));
    } catch (ParseException e) {
      Throwables.propagate(e);
    }
  }

  private int uid = 0;

  private List<SerialTeacher> serialTeachers = Lists.newArrayList();
  private List<SerialRoom> serialRooms = Lists.newArrayList();
  private List<SerialTimeBlock> serialTimeBlocks = Lists.newArrayList();
  private List<SerialCourse> serialCourses = Lists.newArrayList();

  protected SerialPeriod bindPeriod(String name) {
    SerialPeriod period = SerialPeriod.newBuilder()
        .setPeriodId(uid++)
        .setDescription(name)
        .build();
    bind(SerialPeriod.class).annotatedWith(Names.named(name)).toInstance(period);
    return period;
  }

  protected SerialTeacher bindTeacher(String name, SerialPeriod... periods) {
    SerialTeacher.Builder builder = SerialTeacher.newBuilder().setTeacherId(uid++).setName(name);
    for (SerialPeriod period : periods) {
      builder.addAvailablePeriods(period.getPeriodId());
    }
    SerialTeacher teacher = builder.build();
    bind(SerialTeacher.class).annotatedWith(Names.named(name)).toInstance(teacher);
    serialTeachers.add(teacher);
    return teacher;
  }

  protected SerialTimeBlock bindTimeBlock(String name, SerialPeriod... periods) {
    SerialTimeBlock block = SerialTimeBlock.newBuilder()
        .setBlockId(uid++)
        .setDescription(name)
        .addAllPeriods(Arrays.asList(periods))
        .build();
    bind(SerialTimeBlock.class).annotatedWith(Names.named(name)).toInstance(block);
    serialTimeBlocks.add(block);
    return block;
  }

  protected SerialRoom bindRoom(String name, int capacity, SerialPeriod... periods) {
    SerialRoom.Builder builder = SerialRoom.newBuilder()
        .setRoomId(uid++)
        .setName(name)
        .setCapacity(capacity);
    for (SerialPeriod period : periods) {
      builder.addAvailablePeriods(period.getPeriodId());
    }
    SerialRoom room = builder.build();
    bind(SerialRoom.class).annotatedWith(Names.named(name)).toInstance(room);
    serialRooms.add(room);
    return room;
  }

  protected SerialCourse bindCourse(String name, int sections, int periods, int size, SerialTeacher... teachers) {
    SerialCourse.Builder builder = SerialCourse.newBuilder()
        .setCourseId(uid++)
        .setSections(sections)
        .setPeriodLength(periods)
        .setCourseTitle(name)
        .setEstimatedClassSize(size)
        .setMaxClassSize(size);
    for (SerialTeacher t : teachers) {
      builder.addTeacherIds(t.getTeacherId());
    }
    SerialCourse course = builder.build();
    bind(SerialCourse.class).annotatedWith(Names.named(name)).toInstance(course);
    serialCourses.add(course);
    return course;
  }

  @Provides
  SerialProgram createProgram() {
    return SerialProgram.newBuilder()
        .addAllTimeBlocks(serialTimeBlocks)
        .addAllTeachers(serialTeachers)
        .addAllRooms(serialRooms)
        .addAllCourses(serialCourses)
        .build();
  }
}