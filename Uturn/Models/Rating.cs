using System.ComponentModel.DataAnnotations;

namespace Uturn.Models
{
	public class Rating
	{
        public string key { get; set; } = "";

        public string personName { get; set; } = "";

        public string rating { get; set; } = "";

        public string picture { get; set; } = "";
    }
}

