using System.ComponentModel.DataAnnotations;

namespace Uturn.ViewModels
{
	public class TaskViewModel
	{
        public string key { get; set; } = "";

        public string employee { get; set; } = "";

        public string vehicle { get; set; } = "";

        [Required]
        public string type { get; set; } = "";

        [Required]
        public string typeOfGoods { get; set; } = "";

        [Required]
        public string pickupLocation { get; set; } = "";

        [Required]
        public string dropoffLocation { get; set; } = "";

        public string rating { get; set; } = "";

        public string status { get; set; } = "";

        public string uploadedTimestamp { get; set; } = "";

        public string assignedTimestamp { get; set; } = "";

        public string completedTimestamp { get; set; } = "";
    }
}

