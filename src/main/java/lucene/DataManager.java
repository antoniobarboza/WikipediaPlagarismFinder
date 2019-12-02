package lucene;
//package edu.unh.cs;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Page;
import edu.unh.cs.treccar_v2.Data.PageMetadata;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.nio.file.Paths;


public class DataManager {
	private static String eof = "EOF";
	private static ArrayList<String> wantedStrings = initializeList();
	private static String idsPath = "./src/main/java/data/third.txt";
    private static void usage() {
        System.out.println("Command line parameters: (header|pages|outlines|paragraphs|cat) FILE");
        System.exit(-1);
    }
    
    public static void main(String[] args) throws Exception {
        System.setProperty("file.encoding", "UTF-8");

        if (args.length<2)
            usage();
        String mode = args[0];
        if (mode.equals("header")) {
            final String pagesFile = args[1];
            final FileInputStream fileInputStream = new FileInputStream(new File(pagesFile));
            System.out.println(DeserializeData.getTrecCarHeader(fileInputStream));
            System.out.println();
        }
        else if ( mode.equals("cat")) {
        	System.out.println("Cat mode active... Starting..");
        	final String pagesFile = args[1];
        	final FileInputStream fileInputStream = new FileInputStream(new File(pagesFile));
        	final String outFile = idsPath;
        	
        	writePageIdsInCategoryToFile( wantedStrings, DeserializeData.iterableAnnotations(fileInputStream), outFile );
        	System.out.println("Cat mode active... Done..");
        }
        else if (mode.equals("pages")) {
            final String pagesFile = args[1];
            final FileInputStream fileInputStream = new FileInputStream(new File(pagesFile));
            for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream)) {
                System.out.println(page);
                System.out.println();
            }
        } else if (mode.equals("outlines")) {
            final String pagesFile = args[1];
            final FileInputStream fileInputStream3 = new FileInputStream(new File(pagesFile));
            for(Data.Page page: DeserializeData.iterableAnnotations(fileInputStream3)) {
                for (List<Data.Section> sectionPath : page.flatSectionPaths()){
                    System.out.println(Data.sectionPathId(page.getPageId(), sectionPath)+"   \t "+Data.sectionPathHeadings(sectionPath));
                }
                System.out.println();
            }
        } else if (mode.equals("paragraphs")) {
            final String paragraphsFile = args[1];
            final FileInputStream fileInputStream2 = new FileInputStream(new File(paragraphsFile));
            for(Data.Paragraph p: DeserializeData.iterableParagraphs(fileInputStream2)) {
                System.out.println(p);
                System.out.println();
            }
        } else {
            usage();
        }

    }
    private static ArrayList<String> initializeList(){
    	ArrayList<String> build = new ArrayList<String>();
    	//build.add("sport");
    	//build.add("football");  //78000
    	//build.add("soccer");
    	//build.add("baseball");  //14000
    	build.add("cricket");   //10000
    	//build.add("basketball");  //10000
    	//build.add("golf");       //3000
    	//build.add("hockey");     //13000
    	//build.add("mlb");
    	//build.add("nfl");
    	//build.add("nhl");
    	//build.add("nba");
    	//build.add("olympics");
    	//build.add("boxing");
    	//build.add("mvp");
    	return build;
    	
    }
    
    public static ArrayList<String> getDefaultCategoryList() {
    	return wantedStrings;
    }
    
    public static String getIdsPath() {
    	return idsPath;
    }
    
    /**
     * This method is used to check if a single page is a part of the category we are looking for, it then writes the page's id to a file.
     * This method is only called from writePageIdInCategoryToFile
     * 
     * @param category the string that we want the metadata of the page to contain, ignores case
     * @param p the page that we want to check if it is in the category
     */
    
   private static void writeToFileIfInCategory(ArrayList<String> categoriesWanted, Page p, BufferedWriter writer) {
  	 PageMetadata meta = p.getPageMetadata();
  	 String pid = p.getPageId();
  	 ArrayList<String> pagesCategories = meta.getCategoryNames();
  	try {
		writer.write("temp:  " + pid + "\n");
  	} catch (IOException e) {
		e.printStackTrace();
  	}
  	 for(String pageCat: pagesCategories) {
  		 for(String wanted: categoriesWanted) {
  			 if(pageCat.toLowerCase().contains(wanted)) {
  				 try {
  					 //This will show the category that returned this pid as in a category
  					 // we are trying to only get the first 10000 ids becuase it takes 8 hours to index
  					writer.write(wanted + ":  " + pid + "\n");
  					//System.out.println(wanted + ":  " + pid );
  				} catch (IOException e) {
  					e.printStackTrace();
  				}
  				 return;
  			 }
  		 }
  	 }
  	 
   }
   
   /**
    * This method goes through a list of pages and checks if the pages metadata contains any one of the categories provided.
    * @param categoriesWanted list of categories we want, MUST be lowercase
    * @param pages pages to iterate through
    * @param writer the writer to the file that will store all of the page id's that we want to process
    */
   public static void writePageIdsInCategoryToFile(ArrayList<String> categoriesWanted, Iterable<Page> pages, String path) {
	   try {
		   	System.out.println("Begining to build the cat file...");
		   	Files.deleteIfExists(Paths.get(path));
	    	//Create the file to be written to
	    	File defaultRankOutputFile = new File(path);
	    	defaultRankOutputFile.createNewFile();
		   BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		   
		   int count = 0;
		   for(Page p: pages) {
			   writeToFileIfInCategory(categoriesWanted, p, writer);
		   }
		   writer.write(eof);
		   writer.close();
		   System.out.println("Done building the cat file...");
	   } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
   public static HashSet<String> getStopWordsFromFile(String filePath){
	   HashSet<String> ids = null;
	   try {
	   BufferedReader reader = new BufferedReader(new FileReader(filePath));
	   ids = new HashSet<String>();
	   String word = reader.readLine();
	   while( word != null ) {
		   ids.add(word);
		   word = reader.readLine();
	   }
	   //System.out.println("THe size is:" + ids.size());
	   }catch (Exception e) {
		   e.printStackTrace();
	   }
	   return ids;
   }
   /**
    * Tis method will return all of the pageIds that were a part of the categories we gathered from writePageIdsInCategoryToFile
    * @param filePath
    * @return HashSet of page ids that we want to process, all other page ids will be skipped over
 * @throws Exception 
    */
   public static HashSet<String> getPageIdsFromFile(String filePath) throws Exception{
	    BufferedReader reader = new BufferedReader(new FileReader(filePath));
	    HashSet<String> ids = new HashSet<String>();
   	 	String line = reader.readLine();
   	 	line = line.replaceAll("\\s+", " ");
   	 	String[] arrayLine = line.split(" ");
   	 	while(line != null && !arrayLine[0].equals(eof)) {
	    	line = line.replaceAll("\\s+", " ");
	    	arrayLine = line.split(" ");
   	 		ids.add(arrayLine[1]);
   	 		//Get next line
   	 		line = reader.readLine();
   	 		
   	 	}
   	 	reader.close();
	  
	  return ids;
   }
   
   public static ArrayList<String> get25Queries(){
	   ArrayList<String> q = new ArrayList<String>();
	   //enwiki:Left-arm%20orthodox%20spin
	   String s1 = " Left-arm orthodox spin also known as Slow Left Arm Orthodox spin bowlng is a type of Left Arm Finger Leg spin bowling in the sport of cricket. Left-arm orthodox spin is bowled by a left-arm bowler using the fingers to spin the ball from right to left of the cricket pitch (from the bowler's perspective). Left arm orthodox spin bowlers generally attempt to drift the ball in the air into a right-handed batsman, and then turn it away from the batsman (towards off-stump) upon landing on the pitch. The drift and turn in the air are attacking techniques. The left-arm orthodox spin like an off break or off spin is also a bowling action. The major variations of a left-arm spinner are the topspinner (which turns less and bounces higher in the cricket pitch), the arm ball (which does not turn at all, drifts into a right-handed batsman in the direction of the bowler's arm movement; also called a 'floater') and the left-arm spinner's version of a doosra (which turns the other way). The left-arm unorthodox spin like a leg break or leg spin is also a bowling action.";
	   q.add(s1);
	   //enwiki:Leg%20theory
	   String s2 = "Leg theory is a bowling tactic in the sport of cricket. The term leg theory is somewhat archaic and seldom used any longer, but the basic tactic remains a play in modern cricket. Simply put, leg theory involves concentrating the bowling attack at or near the line of leg stump. This may or may not be accompanied by a concentration of fielders on the leg side. The line of attack aims to cramp the batsman, making him play the ball with the bat close to the body. This makes it difficult to hit the ball freely and score runs, especially on the off side. Since a leg theory attack means the batsman is more likely to hit the ball on the leg side, additional fielders on that side of the field can be effective in preventing runs and taking catches. Stifling the batsman in this manner can lead to impatience and frustration, resulting in rash play by the batsman which in turn can lead to a quick dismissal. Leg theory can be a moderately successful tactic when used with both fast bowling and spin bowling, particularly leg spin to right-handed batsmen or off spin to left-handed batsmen. However, because it relies on lack of concentration or discipline by the batsman, it can be risky against patient and skilled players, especially batsmen who are strong on the leg side. The English opening bowlers Sydney Barnes and Frank Foster used leg theory with some success in Australia in 1911-12. In England, at around the same time Fred Root was one of the main proponents of the same tactic. Concentrating attack on the leg stump is considered by many cricket fans and commentators to lead to boring play, as it stifles run scoring and encourages batsmen to play conservatively.";
	   q.add(s2);
	   //enwiki:First-class%20cricket
	   String s3 = "First-class cricket is an official classification of the highest standard international or domestic matches in the sport of cricket. A first-class match is of three or more days' scheduled duration between two sides of eleven players each and is officially adjudged to be worthy of the status by virtue of the standard of the competing teams. Matches must allow for the teams to play two innings each although, in practice, a team might only play one innings or none at all.  First-class cricket (which for this purpose includes all \"important matches\" played before 1895), along with historical single wicket and the modern limited overs forms of List A and Twenty20, is one of the highest standard forms of cricket. The origin of the term \"first-class cricket\" is unknown but it was used loosely before it acquired an official status, effective in 1895, following a meeting of leading English clubs in May 1894. Subsequently, at a meeting of the Imperial Cricket Conference (ICC) in May 1947, it was formally defined on a global basis. A significant omission of the ICC ruling was any attempt to define first-class cricket retrospectively. This has left historians, and especially statisticians, with the problem of how to categorise earlier matches, especially those played before 1895 in Great Britain. The solution put forward by the Association of Cricket Statisticians and Historians (ACS) is to classify all pre-1895 matches of a high standard as important matches. Test cricket, although the highest standard of cricket, is statistically a form of first-class cricket, although the term \"First-class\" is commonly used to refer to domestic competition only. A player's first-class statistics include any performances in Test matches.";
	   q.add(s3);
	   //enwiki:Left-arm%20unorthodox%20spin
	   String s4 = "Left-arm unorthodox spin, also known as slow left arm chinaman, is a type of left arm wrist off spin bowling in the sport of cricket. Left-arm unorthodox spin bowlers use wrist spin to spin the ball, and make it deviate, or \"turn\" from left to right after pitching. The direction of turn is the same as that of a traditional right-handed off spin bowler; however, the ball will usually turn more sharply due to the spin being imparted predominantly by the wrist. Some left-arm unorthodox bowlers also bowl the equivalent of a \"googly\", (or \"wrong'un\"), which turns from right to left on the pitch. The ball turns away from the right-handed batsman, as if the bowler were an orthodox left-arm spinner. In cricketing parlance, the word \"chinaman\" is used to describe the stock delivery of a left-arm \"unorthodox\" spin bowler (though some reserve it for the googly delivery ). The origin of the term is uncertain. One version relates to a Test match played between England and the West Indies at Old Trafford in 1933. Ellis \"Puss\" Achong, a player of Chinese origin, was a left-arm orthodox spinner, playing for the West Indies. He had Walter Robins stumped off a surprise delivery that spun into the right-hander from outside the off stump. As he walked back to the pavilion, Robins reportedly said to the umpire, \"fancy being done by a bloody Chinaman!\", leading to the popularity of the term in England, and subsequently, in the rest of the world. However, it has been suggested that the term originated earlier than this, in Yorkshire. Among noted players who have bowled the chinaman is Denis Compton, who specialised in the delivery when bowling. Although better known for fast bowling and orthodox slow left arm, Garfield Sobers could also use the chinaman to good effect. In cricket's modern era, Brad Hogg is a natural spinner of the ball who popularized the chinaman delivery and has one of the most well-disguised wrong-un's. He was a member of Australia's victorious 2003 and 2007 Cricket World Cup teams, picking up 13 wickets in 2003 and 21 wickets in 2007. Kuldeep Yadav is a Chinaman bowler from India who made a successful debut in the 4th test against Australia in Dharamsala on 25 March 2017 by picking up 4 wickets in the first innings. He is the first Indian left arm chinaman bowler to debut in tests. Paul Adams has been a noted chinaman bowler from South Africa who played 45 test matches between 1995-2004.";
	   q.add(s4);
	   //enwiki:Leg%20spin
	   String s5 = "Leg spin is a type of spin bowling in the sport of cricket. A leg spinner bowls right-arm with a wrist spin action, causing the ball to spin from right to left in the cricket pitch, at the point of delivery. When the ball bounces, the spin causes the ball to deviate sharply from right to left, that is, away from the leg side of a right-handed batsman. The same kind of trajectory, which spins from right to left on pitching, when performed by a left-arm bowler is known as left-arm orthodox spin bowling.   As with all spinners, leg spinners bowl the ball far slower (70–90 km/h or 45–55 mph) than fast bowlers. The fastest leg spinners will sometimes top 100 km/h (60 mph). Leg spinners typically use variations of flight by sometimes looping the ball in the air, allowing any cross-breeze and the aerodynamic effects of the spinning ball to cause the ball to dip and drift before bouncing and spinning (usually called \"turning\") sharply. While very difficult to bowl accurately, good leg spin is considered one of the most threatening types of bowling to bat against, since the flight and sharp turn make the ball's movement extremely hard to read and the turn away from the batsman (assuming he or she is right-handed) is more dangerous than the turn into the batsman generated by an off spinner. Highly skilled leg spin bowlers are also able to bowl deliveries that behave unexpectedly, including the googly, which turns the opposite way to a normal leg break and the topspinner, which does not turn but dips sharply and bounces higher than other deliveries. A few leg spinners such as Abdul Qadir, Anil Kumble, Shane Warne and Mushtaq Ahmed have also mastered the flipper, a delivery that like a topspinner goes straight on landing, but floats through the air before skidding and keeping low, often dismissing batsmen leg before wicket or bowled. Another variation in the arsenal of some leg spinners is the slider, a leg break pushed out of the hand somewhat faster, so that it does not spin as much, but travels more straight on. To grip the ball for a leg-spinning delivery, the ball is placed into the palm with the seam parallel to the palm. The first two fingers then spread and grip the ball, and the third and fourth fingers close together and rest against the side of the ball. The first bend of the third finger should grasp the seam. The thumb resting against the side is up to the bowler, but should impart no pressure. When the ball is bowled, the third finger will apply most of the spin. The wrist is cocked as it comes down by the hip, and the wrist moves sharply from right to left as the ball is released, adding more spin. The ball is tossed up to provide flight. The batsman will see the hand with the palm facing towards them when the ball is released.";
	   q.add(s5);
	   //enwiki:Buddy%20Holly
	   String s6 = "Charles Hardin Holley (September 7, 1936 – February 3, 1959), known as Buddy Holly, was an American musician and singer-songwriter who was a central figure of mid-1950s rock and roll. He was born in Lubbock, Texas, to a musical family during the Great Depression, and learned to play guitar and sing alongside his siblings. His style was influenced by gospel music, country music, and rhythm and blues acts, and he performed in Lubbock with his friends from high school. He made his first appearance on local television in 1952, and the following year he formed the group \"Buddy and Bob\" with his friend Bob Montgomery. In 1955, after opening for Elvis Presley, he decided to pursue a career in music. He opened for Presley three times that year; his band's style shifted from country and western to entirely rock and roll. In October that year, when he opened for Bill Haley & His Comets, he was spotted by Nashville scout Eddie Crandall, who helped him get a contract with Decca Records. Holly's recording sessions at Decca were produced by Owen Bradley. Unhappy with Bradley's control in the studio and with the sound he achieved there, he went to producer Norman Petty in Clovis, New Mexico, and recorded a demo of \"That'll Be the Day\", among other songs. Petty became the band's manager and sent the demo to Brunswick Records, which released it as a single credited to \"The Crickets\", which became the name of Holly's band. In September 1957, as the band toured, \"That'll Be the Day\" topped the US \"Best Sellers in Stores\" chart and the UK Singles Chart. Its success was followed in October by another major hit, \"Peggy Sue\". The album Chirping Crickets, released in November 1957, reached number five on the UK Albums Chart. Holly made his second appearance on The Ed Sullivan Show in January 1958 and soon after, toured Australia and then the UK. In early 1959, he assembled a new band, consisting of future country music star Waylon Jennings (bass), famed session musician Tommy Allsup (guitar), and Carl Bunch (drums), and embarked on a tour of the midwestern U.S. After a show in Clear Lake, Iowa, he chartered an airplane to travel to his next show, in Moorhead, Minnesota. Soon after takeoff, the plane crashed, killing him, Ritchie Valens, The Big Bopper, and pilot Roger Peterson in a tragedy later referred to by Don McLean as \"The Day the Music Died\". During his short career, Holly wrote, recorded, and produced his own material. He is often regarded as the artist who defined the traditional rock-and-roll lineup of two guitars, bass, and drums. He was a major influence on later popular music artists, including Bob Dylan, The Beatles, The Rolling Stones, Eric Clapton, and Elton John. He was among the first artists inducted into the Rock and Roll Hall of Fame, in 1986. Rolling Stone magazine ranked him number 13 in its list of \"100 Greatest Artists\".";
	   q.add(s6);
	   //enwiki:Cardiff%20Arms%20Park
	   String s7 = "Cardiff Arms Park (), also known as The Arms Park and the BT Sport Cardiff Arms Park for sponsorship reasons from September 2014, is situated in the centre of Cardiff, Wales. It is primarily known as a rugby union stadium, but it also has a bowling green. The Arms Park was host to the British Empire and Commonwealth Games in 1958, and hosted four games in the 1991 Rugby World Cup, including the third-place play-off. The Arms Park also hosted the inaugural Heineken Cup Final of 1995–96 and the following year in 1996–97. The history of the rugby ground begins with the first stands appearing for spectators in the ground in 1881–1882. Originally the Arms Park had a cricket ground to the north and a rugby union stadium to the south. By 1969, the cricket ground had been demolished to make way for the present day rugby ground to the north and a second rugby stadium to the south, called the National Stadium. The National Stadium, which was used by Wales national rugby union team, was officially opened on 7 April 1984, however in 1997 it was demolished to make way for the Millennium Stadium in 1999, which hosted the 1999 Rugby World Cup and became the national stadium of Wales. The rugby ground has remained the home of the semi-professional Cardiff RFC yet the professional Cardiff Blues regional rugby union team moved to the Cardiff City Stadium in 2009, but returned three years later. The site is owned by Cardiff Athletic Club and has been host to many sports, apart from rugby union and cricket; they include athletics, association football, greyhound racing, tennis, British baseball and boxing. The site also has a bowling green to the north of the rugby ground, which is used by Cardiff Athletic Bowls Club, which is the bowls section of the Cardiff Athletic Club. The National Stadium also hosted many music concerts including Michael Jackson, David Bowie, Bon Jovi, The Rolling Stones and U2.";
	   q.add(s7);
	   //enwiki:The%20Ashes
	   String s8 = "The Ashes is a Test cricket series played between England and Australia. The Ashes are regarded as being held by the team that most recently won the Test series. The term originated in a satirical obituary published in a British newspaper, The Sporting Times, immediately after Australia's 1882 victory at The Oval, their first Test win on English soil. The obituary stated that English cricket had died, and \"the body will be cremated and the ashes taken to Australia\". The mythical ashes immediately became associated with the 1882–83 series played in Australia, before which the English captain Ivo Bligh had vowed to \"regain those ashes\". The English media therefore dubbed the tour the quest to regain the Ashes. After England had won two of the three Tests on the tour, a small urn was presented to Bligh by a group of Melbourne women including Florence Morphy, whom Bligh married within a year. The contents of the urn are reputed to be the ashes of a wooden bail, and were humorously described as \"the ashes of Australian cricket\". It is not clear whether that \"tiny silver urn\" is the same as the small terracotta urn given to the MCC by Bligh's widow after his death in 1927. The urn has never been the official trophy of the Ashes series, having been a personal gift to Bligh. However, replicas of the urn are often held aloft by victorious teams as a symbol of their victory in an Ashes series. Since the 1998–99 Ashes series, a Waterford Crystal representation of the Ashes urn (called the Ashes Trophy) has been presented to the winners of an Ashes series as the official trophy of that series. Irrespective of which side holds the tournament, the urn remains in the MCC Museum at Lord's; it has however been taken to Australia to be put on touring display on two occasions: as part of the Australian Bicentenary celebrations in 1988, and to accompany the Ashes series in 2006–07. An Ashes series is traditionally of five Tests, hosted in turn by England and Australia at least once every four years. , England holds the Ashes, having won three of the five Tests in the 2015 Ashes series. Australia and England have won 32 series each and five series have been drawn.";
	   q.add(s8);
	   //enwiki:Brian%20Lara
	   String s9 = "Brian Charles Lara, TC, OCC, AM (born 2 May 1969) is a former Trinidadian international cricket player. He is widely acknowledged as one of the greatest batsmen of all time. He topped the Test batting rankings on several occasions and holds several cricketing records, including the record for the highest individual score in first-class cricket, with 501 not out for Warwickshire against Durham at Edgbaston in 1994, which is the only quintuple hundred in first-class cricket history. Lara also holds the record for the highest individual score in a Test innings after scoring 400 not out against England at Antigua in 2004. He is the only batsman to have ever scored a century, a double century, a triple century, a quadruple century and a quintuple century in first class games over the course of a senior career. Lara also shares the test record of scoring the highest number of runs in a single over in a Test match, when he scored 28 runs off an over by Robin Peterson of South Africa in 2003 (matched in 2013 by Australia's George Bailey). Lara's match-winning performance of 153 not out against Australia in Bridgetown, Barbados in 1999 has been rated by Wisden as the second best batting performance in the history of Test cricket, next only to the 270 runs scored by Sir Donald Bradman in The Ashes Test match of 1937. Muttiah Muralitharan, rated as the greatest Test match bowler ever by Wisden Cricketers' Almanack, and the highest wicket-taker in both Test cricket and in One Day Internationals (ODIs), has hailed Lara as his toughest opponent among all batsmen in the world. Lara was awarded the Wisden Leading Cricketer in the World awards in 1994 and 1995 and is also one of only three cricketers to receive the prestigious BBC Overseas Sports Personality of the Year, the other two being Sir Garfield Sobers and Shane Warne. Brian Lara was appointed honorary member of the Order of Australia on 27 November 2009. On 14 September 2012 he was inducted to the ICC's Hall of Fame at the awards ceremony held in Colombo, Sri Lanka as a 2012–13 season inductee along with Australians Glenn McGrath and former England women all-rounder Enid Bakewell. In 2013, Lara received Honorary Life Membership of the MCC becoming the 31st West Indian to receive the honor. Brian Lara is popularly nicknamed as \"The Prince of Port of Spain\" or simply \"The Prince\". He has the dubious distinction of playing in the second highest number of test matches (63) in which his team was on the losing side, just behind Shivnarine Chanderpaul (68).";
	   q.add(s9);
	   //enwiki:Cricket%20World%20Cup
	   String s10 = "The ICC Cricket World Cup is the international championship of One Day International (ODI) cricket. The event is organised by the sport's governing body, the International Cricket Council (ICC), every four years, with preliminary qualification rounds leading up to a finals tournament. The tournament is one of the world's most viewed sporting events and is considered the \"flagship event of the international cricket calendar\" by the ICC. The first World Cup was organised in England in June 1975, with the first ODI cricket match having been played only four years earlier. However, a separate Women's Cricket World Cup had been held two years before the first men's tournament, and a tournament involving multiple international teams had been held as early as 1912, when a triangular tournament of Test matches was played between Australia, England and South Africa. The first three World Cups were held in England. From the 1987 tournament onwards, hosting has been shared between countries under an unofficial rotation system, with fourteen ICC members having hosted at least one match in the tournament. The finals of the World Cup are contested by the ten full members of the ICC (all of which are Test-playing teams) and a number of teams made up from associate and affiliate members of the ICC, selected via the World Cricket League and a later qualifying tournament. A total of twenty teams have competed in the eleven editions of the tournament, with fourteen competing in the latest edition in 2015. Australia has won the tournament five times, with the West Indies, India (twice each), Pakistan and Sri Lanka (once each) also having won the tournament. The best performance by a non-full-member team came when Kenya made the semi-finals of the 2003 tournament.";
	   q.add(s10);
	   //enwiki:Ian%20Botham
	   String s11 = " Sir Ian Terence Botham, OBE (born 24 November 1955) is an English former first-class cricketer, active 1974–1993, who played mainly for Somerset and also for Worcestershire, Durham and Queensland. He represented England in 102 Test matches and 116 Limited Overs Internationals. He later became a cricket commentator. He was a right-handed batsman and, as a right arm fast-medium bowler, was noted for his swing bowling. He generally fielded close to the wicket, predominantly in the slips. Skilled in all three disciplines, Botham was a genuine all-rounder. In Test cricket, he scored 5,200 runs including 14 centuries with a highest score of 208; he took 383 wickets with a best return of eight for 34; and he held 120 catches. From 1986 to 1988, he held the world record for the highest number of career wickets in Test cricket. He took five wickets in an innings (5wI) 27 times and 10 wickets in a match (10wM) four times. In 1980, he became the second player in Test history to complete the \"match double\" of scoring 100 runs and taking 10 wickets in the same match; his feat included a century and he was the first of only two players to score a century and take ten wickets in the same Test match. In all first-class cricket, he scored 19,399 runs including 38 centuries with a highest score of 228; he took 1,172 wickets with the same best return of eight for 34, 59 5wI and eight 10wM; he held 354 catches. On 8 August 2009, he was inducted into the ICC Cricket Hall of Fame. Botham has at times been involved in controversy including a highly publicised court case involving rival all-rounder Imran Khan and an ongoing dispute with the Royal Society for the Protection of Birds (RSPB). These incidents, allied to his on-field success, have attracted media attention, especially from the tabloid press. Botham has made effective use of the fame given to him by the publicity because he is actively concerned about leukaemia in children and has undertaken several long distance walks to raise money for research into the disease. These efforts have been highly successful and have realised millions of pounds for Bloodwise, of which he became president. In recognition of his services to charity, he was awarded a knighthood in the 2007 New Years Honours List. Botham has a wide range of sporting interests outside cricket. He was a talented footballer at school and had to choose between cricket and football as a career. He chose cricket but, even so, he did play professional football for a few seasons and made eleven appearances in the Football League for Scunthorpe United. He is a keen golfer and his other pastimes include angling and shooting.";
	   q.add(s11);
	   //enwiki:Hansie%20Cronje
	   String s12 = "Wessel Johannes \"Hansie\" Cronje (25 September 1969 – 1 June 2002) was a South African cricketer and captain of the South African national cricket team in the 1990s. He died in a plane crash in 2002. He was voted the 11th greatest South African in 2004 despite having been banned from cricket for life due to his role in a match-fixing scandal.";
	   q.add(s12);
	   //enwiki:Melbourne%20Cricket%20Ground
	   String s13 = "The Melbourne Cricket Ground (MCG), also known simply as \"The G\", is an Australian sports stadium located in Yarra Park, Melbourne, Victoria. Home to the Melbourne Cricket Club, it is the 10th-largest stadium in the world, the largest in Australia, the largest in the Southern Hemisphere, the largest cricket ground by capacity, and has the tallest light towers of any sporting venue. The MCG is within walking distance of the city centre and is served by the Richmond railway station, Richmond, and the Jolimont railway station, East Melbourne. It is part of the Melbourne Sports and Entertainment Precinct. Since it was built in 1853, the MCG has been in a state of almost constant renewal. It served as the centrepiece stadium of the 1956 Summer Olympics, the 2006 Commonwealth Games and two Cricket World Cups: 1992 and 2015. It is also famous for its role in the development of international cricket; it was the venue for both the first Test match and the first One Day International, played between Australia and England in 1877 and 1971 respectively. The annual Boxing Day Test is one of the MCG's most popular events. Referred to as \"the spiritual home of Australian rules football\" for its strong association with the sport since it was codified in 1859, it hosts Australian Football League (AFL) matches in the winter, with at least one game held there in most (if not all) rounds of the premiership season. The stadium fills to capacity for the AFL Grand Final. Home to the National Sports Museum, the MCG has hosted other major sporting events, including international rules football matches between Australia and Ireland, international rugby union matches, State of Origin (rugby league) games, and FIFA World Cup qualifiers. Concerts and other cultural events are also held at the venue, with the record attendance standing at around 130,000 for a Billy Graham evangelistic crusade in 1959. Grandstand redevelopments and occupational health and safety legislation have limited the maximum seating capacity to approximately 95,000 with an additional 5,000 standing room capacity, bringing the total capacity to 100,024. The MCG is listed on the Victorian Heritage Register and was included on the Australian National Heritage List in 2005. Journalist Greg Baum called it \"a shrine, a citadel, a landmark, a totem\" that \"symbolises Melbourne to the world\".";
	   q.add(s13);
	   //enwiki:Bodyline
	   String s14 = "Bodyline, also known as fast leg theory bowling, was a cricketing tactic devised by the English cricket team for their 1932–33 Ashes tour of Australia, specifically to combat the extraordinary batting skill of Australia's Don Bradman. A bodyline delivery was one where the cricket ball was bowled towards the body of the batsman on the line of the leg stump, in the hope of creating leg-side deflections that could be caught by one of several fielders in the quadrant of the field behind square leg. This was considered by many to be intimidatory and physically threatening, to the point of being unfair in a game once supposed to have gentlemanly traditions, although commercialisation of the game had subsequently tended to elevate the principle of \"win at all costs\" above traditional ideals of sportsmanship. Although no serious injuries arose from any short-pitched deliveries while a leg theory field was set, the tactic still led to considerable ill feeling between the two teams, with the controversy eventually spilling into the diplomatic arena. Over the next two decades, several of the Laws of Cricket were changed to prevent this tactic being repeated. Law 41.5 states \"At the instant of the bowler's delivery there shall not be more than two fielders, other than the wicket-keeper, behind the popping crease on the on side,\" commonly referred to as being \"behind square leg\". Additionally, Law 42.6(a) includes: \"The bowling of fast short pitched balls is dangerous and unfair if the umpire at the bowler's end considers that by their repetition and taking into account their length, height and direction they are likely to inflict physical injury on the striker\". The occasional short-pitched ball aimed at the batsman (a bouncer) has never been illegal and is still in widespread use as a tactic.";
	   q.add(s14);
	   //enwiki:Geoff%20Hurst
	   String s15 = "Sir Geoffrey Charles Hurst MBE (born 8 December 1941) is a former England international footballer. A striker, he remains the only man to score a hat-trick in a World Cup final as England recorded a 4–2 victory over West Germany at the old Wembley in 1966. He began his career with West Ham United, where he scored 242 goals in 500 first team appearances. There he won the FA Cup in 1964 and the European Cup Winners' Cup 1965. He was sold to Stoke City in 1972 for £80,000. After three seasons with Stoke he finished his Football League career with West Bromwich Albion in 1976. Hurst went to play football in Ireland (Cork Celtic) and the USA (Seattle Sounders) before returning to England to manage non-league Telford United. He also coached in the England set-up before an unsuccessful stint as Chelsea manager from 1979 to 1981. He later coached Kuwait SC before leaving the game to concentrate on his business commitments. In total he scored 24 goals in 49 England appearances, and as well as success in the 1966 World Cup he also appeared at UEFA Euro 1968 and the 1970 FIFA World Cup. He also had a brief cricket career, making one First-class appearance for Essex in 1962, before concentrating on football.";
	   q.add(s15);
	   //enwiki:Flipper%20(cricket)
	   String s16 = "The flipper is the name of a particular bowling delivery used in cricket, generally by a leg spin bowler. In essence it is a back spin ball. Squeezed out of the front of the hand with the thumb and first and second fingers, it keeps deceptively low after pitching and can accordingly be very difficult to play. The flipper is comparable to a riseball in fast-pitch softball.  By putting backspin on the ball the Magnus effect results in air travelling over the top of the ball quickly and cleanly whilst air travelling under the ball is turbulent. The lift produced means that the ball drops slower and travels further than a normal delivery. The slower descent also results in the ball bouncing lower. The flipper is bowled on the opposite side to a slider, much in the same way that the top-spinner is bowled. On release, the bowler 'pinches' or clicks the thumb and forefinger, causing the ball to come out underneath the hand. There must be sufficient tension in the wrist and fingers to impart a good helping of backspin or underspin. In doing so the flipper will float on towards the batsman and land on a fuller length than he anticipated, often leaving him caught on the back foot when he wrongly assumes it to be a pullable or a cuttable ball. The back spin or underspin will cause the ball to hurry on at great pace with very little bounce, though this may be harder to achieve on softer wickets. A series of normal leg spinners or topspinners, with their dropping looping flight, will have the batsman used to the ball pitching on a shorter length. The batsman may wrongly assume that the flipper will drop and loop like a normal overspinning delivery, resulting in the ball pitching under the bat and going on to either hit the stumps or result in leg before wicket. Much of the effectiveness of the flipper is attributable to the \"pop\", that is, the extra pace and change in trajectory that is imparted to the ball when it is squeezed out of the bowler's hand. Occasionally, the term 'flipper' has been used to describe other types of deliveries. The Australian leg spinner Bob Holland employed a back spinning ball that he simply pushed backwards with the heel of his palm. Sometimes this form of front-hand flipper is called a \"zooter\". It is easier to bowl but not as effective as the amount of backspin is much less.";
	   q.add(s16);
	   /**
	   //enwiki:Category:Short%20form%20cricket
	   String s18 = "This category is designed to hold all variations on the traditional forms of cricket and so includes such diverse alternatives as beach cricket, cricket card or computer games, French cricket, street cricket, etc.Category:Forms of cricket";
	   //q.add(s18);
	   //enwiki:Mozambique%20national%20cricket%20team
	   String s19 = "The Mozambique national cricket team is the team that represents the country of Mozambique in international cricket matches. The Mozambique national cricket team, which is administrated by the Mozambican Cricket Association, is an affiliate member of the International Cricket Council (ICC). Mozambique is also a member of the African Cricket Association. The Mozambique national cricket team has competed in the World Cricket League Africa Region and the ICC Africa Twenty20 Championship.";
	   //.add(s19);
	   //enwiki:Japan%20Cricket%20Association
	   String s20 = "Japan Cricket Association, a Japanese Non-Profit Organization, is the governing body for cricket in Japan. It was originally formed in 1984 and registered as NPO in 2001.Japan Cricket Association operates the Japanese cricket team and organises domestic cricket in Japan. Japan Cricket Association has been an Associate Member of International Cricket Council since 2005, belonging to the East-Asia Pacific region, under the International Cricket Council's development program. The Headquarter of the Japan Cricket Association are in Minato-ku, Tokyo, JAPAN.";
	   //q.add(s20);
	   //enwiki:Cricket%20Scotland
	   String s21 = "Cricket Scotland, formerly known as the Scottish Cricket Union, is the governing body of the sport of cricket in Scotland. The body is based at the National Cricket Academy, Edinburgh.The SCU was formed in 1908, but underwent a major restructuring in 2001 including a name change. It became an International Cricket Council member in 1994 as an Associate nation. It has three sub-associations: East of Scotland Cricket Association, Western District Cricket Union and the Aberdeenshire Cricket Association.";
	   //q.add(s21);
	   //enwiki:CricketArchive
	   String s22 = "CricketArchive is a cricket database website that aims to provide a complete archive of records relating to the sport of cricket. It claims to be the most comprehensive cricket database on the internet, including scorecards for all matches of first-class cricket (including Test cricket), List A cricket (including One Day Internationals), Women's Test cricket and Women's One-day Internationals, ICC Trophy, and international Under-19 cricket \"Test\" and one-day cricket matches also blind cricket matches.It is working on completing its coverage of English Second XI and other matches, including extensive coverage of UK club cricket.In 2017, the owners of the service decided to introduce paid access to the website.";
	   //q.add(s22);
	   //enwiki:Wales%20national%20cricket%20team
	   String s23 = "The Welsh cricket team () is the representative cricket team for Wales. Despite Wales and England being represented in Test Cricket by the England team, the Welsh cricket team continues to play short form cricket periodically.";
	   //q.add(s23);
	   //enwiki:Cricket%20nets
	   String s24 = "A cricket net is a practice net used by batsmen and bowlers to warm up and/or improve their cricketing techniques.  Cricket nets consist of a cricket pitch (natural or artificial) which is enclosed by cricket nets on either side, to the rear and optionally the roof.  The bowling end of the net is left open.  Cricket nets are the cricket equivalent of baseball's batting cages, though fundamentally different, as baseball cages provide complete ball containment, whereas cricket nets do not.";
	   //q.add(s24);
	   //enwiki:Adam%20Dale
	   String s25 = "Adam Craig Dale (born 30 December 1968, in Ivanhoe, Victoria) is a former Australian cricketer who played in 2 Tests and 30 ODIs from 1997 to 2000. He played in first-class and List A cricket for Queensland Bulls and in club cricket for North Melbourne Cricket Club, Heidelberg Cricket Club, Northcote Cricket Club, Old Paradians Cricket Club and Research Cricket Club.From a short, ambling run-up, Dale delivered medium-paced outswingers with nagging accuracy. He therefore become known more as an economical bowler in one-day cricket, although he was selected for two Tests throughout his career and was very successful for Queensland in the first-class arena. He is best remembered however for taking one of the greatest catches ever seen in the game of cricket whilst playing for Queensland in the summer of 1997/98.He played grade cricket for the Wynnum-Manly Cricket Club in Brisbane, and premier cricket for Northcote, Heidelberg Cricket Club, North Melbourne and Melbourne in Melbourne, over a long career which spanned twenty-six years from 1985/86 to 2010/11.";
	   //q.add(s25);
	    */
	   return q;
   }
   
   public static String convertToId(String docText) {
		  return docText.replaceAll("\\s","%20%");
	  }
   
   public static String deconvertFromId(String  docText) {
	   return docText.replaceAll("%20%"," ");
   }
   
}
