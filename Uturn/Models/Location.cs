using System.ComponentModel.DataAnnotations;

namespace Uturn.Models
{
	public class Location
	{
        public string key { get; set; } = "";

        [Required]
        public string name { get; set; } = "";

        [Required]
        public string type { get; set; } = "";

        [Required]
        public string address { get; set; } = "";
    }
}

