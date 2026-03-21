using System.Collections.Generic;
using QuickStartDemo.Entities;

namespace QuickStartDemo.Repositories.Interfaces
{
    public interface IMessageRepository
    {
        bool AddMessages(List<MessageEntity> messages);
    }
}