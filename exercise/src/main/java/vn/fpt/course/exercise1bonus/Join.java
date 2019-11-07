package vn.fpt.course.exercise1bonus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
public class Join {
    public static class PostMapper extends Mapper <Object, Text, Text, Text>
    {
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException
        {
            String record = value.toString();
            String[] parts = record.split("\t");
            context.write(new Text(parts[0]), new Text("left   "+ parts[1] + "   " + parts[2]));
        }
    }

    public static class IndusMapper extends Mapper <Object, Text, Text, Text>
    {
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException
        {
            String record = value.toString();
            String[] parts = record.split(",");
            context.write(new Text(parts[0]), new Text("right   " + parts[1]));
        }
    }

    public static class ReduceJoinReducer extends Reducer <Text, Text, Text, Text>
    {
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException
        {
            String name = "";
            int count = 0;
            List<String> queue = new ArrayList<String>();
            for (Text t : values)
            {
                String parts[] = t.toString().split("  ");
                if (parts[0].equals("left"))
                {
                    String value = parts[1] + "  " + parts[2];
                    if(name.equals("")){
                        queue.add(value);
                    }
                    else
                        context.write(new Text(name), new Text(value));
                }
                else if (parts[0].equals("right"))
                {
                    name = parts[1];
                    for(String value: queue){
                        context.write(new Text(name), new Text(value));
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = new Job(conf, "Reduce-side join");
        job.setJarByClass(Join.class);
        job.setReducerClass(ReduceJoinReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        MultipleInputs.addInputPath(job, new Path(args[0]),TextInputFormat.class, PostMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]),TextInputFormat.class, IndusMapper.class);
        Path outputPath = new Path(args[2]);

        FileOutputFormat.setOutputPath(job, outputPath);
        outputPath.getFileSystem(conf).delete(outputPath);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}