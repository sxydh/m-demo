using System.Collections.Generic;
using QuickStartDemo.Entities;
using QuickStartDemo.Helpers;
using QuickStartDemo.Repositories.Interfaces;

namespace QuickStartDemo.Repositories.Implementations
{
    public class MessageRepository : IMessageRepository
    {

        private readonly DbContext _dbContext;
        
        public MessageRepository(DbContext dbContext)
        {
            _dbContext = dbContext;
        }
        
        public bool AddMessages(List<MessageEntity> messages)
        {
            var storage = _dbContext.Db.Storageable(messages)
                .WhereColumns(it => it.MessageId)
                .ToStorage();
            return storage.AsInsertable.ExecuteCommand() > 0;
        }
    }
}