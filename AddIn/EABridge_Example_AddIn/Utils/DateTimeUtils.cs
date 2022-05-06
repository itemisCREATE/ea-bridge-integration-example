using System;

namespace EABridge_Example_AddIn.Utils
{
    public static class DateTimeUtils
    {
        private static long SecInMilisec = 1000;
        private static long MinInMilisec = SecInMilisec * 60;
        private static long HourInMilisec = MinInMilisec * 60;
        private static long DayInMilisec = HourInMilisec * 24;
        private static long YearInMilisec = DayInMilisec * 365;

        public static long GetTimeDifferenceInMilisec(DateTime date1, DateTime date2)
        {
            long difference = date1.GetDateTimeInMilisec() - date2.GetDateTimeInMilisec();
            return Math.Abs(difference);
        }

        public static long GetDateTimeInMilisec(this DateTime dateTime)
        {
            return (dateTime.Year * YearInMilisec) + (dateTime.DayOfYear * DayInMilisec) + (dateTime.Hour * HourInMilisec) + (dateTime.Minute * MinInMilisec)
                + (dateTime.Second * SecInMilisec) + dateTime.Millisecond;
        }

        public static string GetHumanReadableDuration(long durationInMiliseconds)
        {
            if (durationInMiliseconds < 2000)
                return durationInMiliseconds + " ms";
            if (durationInMiliseconds < 100 * 1000)
                return (durationInMiliseconds / 1000) + " sec";
            return (durationInMiliseconds / 1000 / 60) + " min";
        }
    }
}

