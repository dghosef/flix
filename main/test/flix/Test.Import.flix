import java.lang.{StringBuffer, CharSequence => ASeqOfChars}
import java.lang.{StringBuffer => RenamedBuffer1}

namespace Test/Import {

  // Don't cause an error because different names from above
  import java.lang.{StringBuffer => RenamedBuffer2, CharSequence}

  @test
  def testImport01(): Bool \ IO =
    import new java.lang.StringBuffer(String): StringBuffer as newStringBuffer;
    import java.lang.StringBuffer.subSequence(Int32, Int32): ASeqOfChars;
    let sb = newStringBuffer("Hello world");
    let subStr = subSequence(sb, 6, 11) as String;
    Assert.eq(subStr, "world")

  @test
  def testImport02(): Bool \ IO =
    import new java.lang.StringBuffer(String): RenamedBuffer2 as newStringBuffer;
    import java.lang.StringBuffer.subSequence(Int32, Int32): CharSequence;
    let sb = newStringBuffer("Hello world");
    let subStr = subSequence(sb, 6, 11) as String;
    Assert.eq(subStr, "world")
}

namespace Test/Import {
  // Doesn't cause an error because different scope from above
  import java.lang.CharSequence

  @test
  def testImport03(): Bool \ IO =
    import new java.lang.StringBuffer(String): RenamedBuffer1 as newStringBuffer;
    import java.lang.StringBuffer.subSequence(Int32, Int32): CharSequence;
    let sb = newStringBuffer("Hello world");
    let subStr = subSequence(sb, 6, 11) as String;
    Assert.eq(subStr, "world")
}

namespace Test/Import {
  // Doesn't cause an error because different scope from above
  import java.lang.CharSequence
}